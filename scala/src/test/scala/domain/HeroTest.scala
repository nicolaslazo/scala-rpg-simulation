package domain

import domain.ItemSlot.{Head, LeftHand, Torso}
import domain.Stat.*
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable.HashMap
import scala.util.Try

class HeroTest extends AnyFlatSpec {
    "Un heroe" should "redondear stats no positivos a 1" in {
        val hero: Hero = Hero(HashMap(Health -> 2, Strength -> 1, Intelligence -> -1))

        assert(hero.stat(Health) === 2)
        assert(hero.stat(Strength) === 1)
        assert(hero.stat(Speed) === 1)
        assert(hero.stat(Intelligence) === 1)
    }

    "Un heroe empleado" should "tener sus stats modificados" in {
        val hero: Hero = Hero(HashMap(Health -> 1, Strength -> 2, Speed -> 3, Intelligence -> 4), Some(Guerrero))

        assert(hero.stat(Health) === 11)
        assert(hero.stat(Strength) === 17)
        assert(hero.stat(Speed) === 3)
        assert(hero.stat(Intelligence) === 1)
    }

    "Un heroe debil" should "ser incapaz de equiparse el casco vikingo" in {
        assert(Hero().equip(CascoVikingo, Head).isFailure)
    }

    "Un heroe fuerte" should "ser capaz de equiparse el casco vikingo" in {
        val hero: Hero = Hero(baseAttributes = HashMap(Strength -> 100))
        val equippedHero: Try[Hero] = hero.equip(CascoVikingo, Head)

        assert(equippedHero.isSuccess)
        assert(equippedHero.get.equipment == HashMap(Head -> CascoVikingo))
    }

    "Un heroe" should "ser incapaz de equiparse un casco en el torso" in {
        assert(Hero(baseAttributes = HashMap(Strength -> 100)).equip(CascoVikingo, Torso).isFailure)
    }

    "Un heroe" should "poder equipar items que checkean su trabajo" in {
        assert(Hero(job = Some(Mago))
            .equip(PalitoMagico, LeftHand)
            .isSuccess)
    }

    "Un heroe" should "ser afectado por los items que tiene equipados" in {
        val hero: Hero = Hero(baseAttributes = HashMap(Strength -> 100), job = Some(Mago))
            .equip(CascoVikingo, Head)
            .flatMap(_.equip(PalitoMagico, LeftHand))
            .get

        assert(hero.stat(Health) == 10) // CascoVikingo
        assert(hero.stat(Strength) == 100 - 20) // Base - Mago
        assert(hero.stat(Intelligence) == 20 + 20) // Mago + PalitoMagico
    }
}