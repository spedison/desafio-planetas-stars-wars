package com.spedison.planetasstarwars.vo


import javax.persistence.*

@Entity
@Table(name = "tb_regiao")
data class Regiao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column(nullable = false, unique = false)
    var nome: String,

    @ManyToOne
    var planeta: Planeta?,

    var ativo: Boolean = true,

    @ManyToOne
    var clima: Clima,

    @ManyToOne
    var terreno: Terreno
)