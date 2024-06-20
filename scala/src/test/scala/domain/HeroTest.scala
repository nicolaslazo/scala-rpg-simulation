package domain

import cats.syntax.option.*
import domain.Stat.*
import domain.equipment.ItemSlot.*
import domain.equipment.*
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable.HashMap
import scala.util.{Success, Try}

class HeroTest extends AnyFlatSpec {
    private val mage = Hero(job = Mago.some)

    "Un héroe" should "redondear stats no positivos a 1" in {
        val hero: Hero = Hero(StatBlock(Health -> 2, Strength -> 1, Intelligence -> -1))

        assert(hero.stat(Health) === 2)
        assert(hero.stat(Strength) === 1)
        assert(hero.stat(Speed) === 1)
        assert(hero.stat(Intelligence) === 1)
    }

    "Un héroe empleado" should "tener sus stats modificados" in {
        val hero: Hero = Hero(StatBlock(Health -> 1, Strength -> 2, Speed -> 3, Intelligence -> 4), Some(Guerrero))

        assert(hero.stat(Health) === 11)
        assert(hero.stat(Strength) === 17)
        assert(hero.stat(Speed) === 3)
        assert(hero.stat(Intelligence) === 1)
    }

    "Un héroe debil" should "ser incapaz de equiparse el casco vikingo" in {
        assert(Hero().equip(CascoVikingo, Head).isFailure)
    }

    "Un héroe fuerte" should "ser capaz de equiparse el casco vikingo" in {
        val hero: Hero = Hero(baseAttributes = StatBlock(Strength -> 100))
        val equippedHero: Try[Hero] = hero.equip(CascoVikingo, Head)

        assert(equippedHero.isSuccess)
        assert(equippedHero.get.equipment == HashMap(Head -> CascoVikingo))
    }

    "Un héroe" should "ser incapaz de equiparse un casco en el torso" in {
        assert(Hero(baseAttributes = StatBlock(Strength -> 100)).equip(CascoVikingo, Torso).isFailure)
    }

    "Un héroe" should "poder equipar items que checkean su trabajo" in {
        assert(mage.equip(PalitoMagico, LeftHand).isSuccess)
    }

    "Un héroe" should "tener los stats modificados por los items que tiene equipados" in {
        val hero: Try[Hero] = for {
            baseHero <- Success(Hero(baseAttributes = StatBlock(Strength -> 100), job = Some(Mago)))
            heroWithHelmet <- baseHero.equip(CascoVikingo, Head)
            finalHero <- heroWithHelmet.equip(PalitoMagico, LeftHand)
        } yield finalHero

        assert(hero.get.stat(Health) == 10) // CascoVikingo
        assert(hero.get.stat(Strength) == 100 - 20) // Base - Mago
        assert(hero.get.stat(Intelligence) == 20 + 20) // Mago + PalitoMagico
    }

    "Un héroe" should "ser afectado por items que ocupan las dos manos una sola vez" in {
        assert(Hero().equip(ArcoViejo, BothHands).map(_.stat(Strength)).getOrElse(0) == 2)
    }

    "Un héroe" should "desequipar cualquier item que requiere dos manos cuando se equipa uno de una sola" in {
        assert(!(for {
            hero <- Try(Hero())
            heroWithBow <- hero.equip(ArcoViejo, BothHands)
            heroWithSword <- heroWithBow.equip(EspadaDeLaVida, LeftHand)
        } yield heroWithSword.equipment.contains(RightHand)).get)
    }

    "Un héroe" should "ser afectado por los items que equipa" in {
        //        val talismanMage = mage
        //            .equip(TalismanDeDedicacion, Neck)
        //            .flatMap(_.equip(Item(slot = Neck), target = Neck))
        //            .get

        // TODO: Arreglar
        //        assert(mage.stat(Health) == 2)
        //        assert(mage.stat(Strength) == -18)
        //        assert(mage.stat(Speed) == 2)
        //        assert(mage.stat(Intelligence) == 22)
    }

    "Un héroe" should "ser incapaz de equiparse un item en SingleHand" in {
        assert(Hero().equip(Item(slot = SingleHand), SingleHand).isFailure)
    }

    "Un héroe" should "poder reportar su stat principal" in {
        assert(Hero(job = Guerrero.some).mainStatPoints.get == 15)
    }

    "Un héroe" should "poder reportar cómo quedaría su stat principal si se equipa un item" in {
        assert(mage.withItemEquippedProjection(PalitoMagico, LeftHand).get.pointsProjection == 40)
    }
}