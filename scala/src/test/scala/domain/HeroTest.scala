package domain

import org.scalatest.flatspec.AnyFlatSpec

class HeroTest extends AnyFlatSpec {
    "Un heroe" should "redondear stats no positivos a 1" in {
        val unHeroe: Hero = new Hero(StatBlock(2, 1, 0, -1))

        assert(unHeroe.health() === 2)
        assert(unHeroe.strength() === 1)
        assert(unHeroe.speed() === 1)
        assert(unHeroe.intelligence() === 1)
    }
}