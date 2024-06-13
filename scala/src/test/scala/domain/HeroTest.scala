package domain

import domain.Stat.*
import org.scalatest.flatspec.AnyFlatSpec

class HeroTest extends AnyFlatSpec {
    "Un heroe" should "redondear stats no positivos a 1" in {
        val hero: Hero = Hero(StatBlock(health = 2, strength = 1, intelligence = -1))

        assert(hero.stat(Health) === 2)
        assert(hero.stat(Strength) === 1)
        assert(hero.stat(Speed) === 1)
        assert(hero.stat(Intelligence) === 1)
    }

    "Un heroe empleado" should "tener sus stats modificados" in {
        val hero: Hero = Hero(StatBlock(health = 1, strength = 2, speed = 3, intelligence = 4), Some(Guerrero))

        assert(hero.stat(Health) === 11)
        assert(hero.stat(Strength) === 17)
        assert(hero.stat(Speed) === 3)
        assert(hero.stat(Intelligence) === 1)
    }
}