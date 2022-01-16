package com.spedison.planetasstarwars.service

import com.spedison.planetasstarwars.dto.clima.ViewClimaDTO
import com.spedison.planetasstarwars.dto.clima.FormClimaDTO
import com.spedison.planetasstarwars.exception.RegisterConstraintException
import com.spedison.planetasstarwars.exception.RegisterNotFoundException
import com.spedison.planetasstarwars.map.GenericMapperInterface
import com.spedison.planetasstarwars.repository.ClimaRepository
import com.spedison.planetasstarwars.vo.Clima
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.streams.toList

@Component
class ClimaService(
    val repository: ClimaRepository,
    var mapper: GenericMapperInterface<Clima, FormClimaDTO, ViewClimaDTO>,
) {
    private val nomeClasse : String = this::class.simpleName?:""

    @Cacheable("climaTodos")
    fun listaTodos(): List<ViewClimaDTO> =
        repository
            .findAllByAtivo(true)
            .stream()
            .map(mapper::mappeiaParaDTO)
            .toList()

    @Cacheable("climaUnico")
    fun listaUm(id: Long): ViewClimaDTO {

        val clima = repository.findByIdAndAtivo(id, true)

        clima.orElseThrow {
            RegisterNotFoundException(
                "listaUm",
                id = id,
                nomeClasse = nomeClasse
            )
        }

        return mapper.mappeiaParaDTO(clima.get())
    }

    @Transactional
    @CacheEvict(value = ["climaTodos", "climaUnico"], allEntries = true)
    fun adiciona(valor: FormClimaDTO): ViewClimaDTO {
        var clima: Clima = mapper.mappeiaParaClasse(valor)
        repository.save(clima)
        return mapper.mappeiaParaDTO(clima)
    }

    @Transactional
    @CacheEvict(value = ["climaTodos", "climaUnico"], allEntries = true)
    fun edita(form: FormClimaDTO, id: Long): ViewClimaDTO {

        var clima: Optional<Clima> = repository.findByIdAndAtivo(id, true)

        clima.orElseThrow {
            RegisterNotFoundException(
                "edita",
                id = id,
                nomeClasse = nomeClasse
            )
        }

        clima.get().let {
            it.nome = form.nome
            it.viavelParaVida = form.viavelParaVida as Boolean
            repository.save(it)
        }

        return mapper.mappeiaParaDTO(clima.get())
    }

    @Transactional
    @CacheEvict(value = ["climaTodos", "climaUnico"], allEntries = true)
    fun apaga(id: Long): ViewClimaDTO {

        var clima: Optional<Clima> = repository.findByIdAndAtivo(id, true)

        clima.orElseThrow {
            RegisterNotFoundException(
                "apaga",
                id = id,
                nomeClasse = nomeClasse
            )
        }

        if (repository.countPlanetasAtivosByID(clima.get().id ?: -1) > 0)
            throw RegisterConstraintException("Registro de Clima está sendo utilizado no planeta.",
                clima.get().id ?: -1,
                nomeClasse)

        clima.get().let {
            it.ativo = false
            repository.save(it)
        }

        return mapper.mappeiaParaDTO(clima.get())
    }
}