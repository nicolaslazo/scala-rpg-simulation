package domain

import cats.syntax.option.*
import domain.Stat.*
import domain.equipment.ItemSlot.{Head, LeftHand, RightHand, SingleHand}
import domain.equipment.{CascoVikingo, Item}
import org.scalatest.flatspec.AnyFlatSpec

val mage: Hero = Hero(job = Mago.some)

class TeamTest extends AnyFlatSpec {
    "Un equipo" should "usar un criterio para seleccionar el mejor miembro" in {
        val goodHero: Hero = Hero(StatBlock(Speed -> 2))

        assert(Team(name = "", members = Set(goodHero, Hero())).bestMember(_.stat(Speed)).get == goodHero)
    }

    "Un equipo" should "decir que no tiene un mejor miembro si está vacío" in {
        assert(Team("").bestMember(_ => 0).isEmpty)
    }

    "Un equipo sin miembros" should "automáticamente vender cualquier item que recibe" in {
        assert(Team("").addItem(CascoVikingo).earnings == 100)
    }

    "Un equipo con un item nuevo" should "venderlo si ningún miembro lo puede equipar" in {
        val teamWithWeakHero = Team("", Set(Hero()))

        assert(teamWithWeakHero.addItem(CascoVikingo).earnings == 100)
    }

    "Un equipo con un item nuevo" should "venderlo si equiparlo no beneficia a ninguno de los miembros" in {
        val buffMage = Hero(baseAttributes = StatBlock(Strength -> 100), job = Mago.some)

        assert(Team("", members = Set(buffMage)).addItem(CascoVikingo).earnings == 100)
    }

    "Un equipo con un item de una sola mano" should "equiparlo en la mano correcta" in {
        val badItem = Item(StatBlock(Intelligence -> 1), slot = SingleHand)
        val midItem = badItem.copy(modifiers = StatBlock(Intelligence -> 2), slot = SingleHand)
        val goodItem = badItem.copy(modifiers = StatBlock(Intelligence -> 3), slot = SingleHand)

        val badlyEquippedMage = mage.equip(badItem, RightHand).flatMap(_.equip(midItem, LeftHand)).get
        val mageTeam = Team("", members = Set(badlyEquippedMage))

        assert(mageTeam.addItem(goodItem).members.last.equipment(RightHand) == goodItem)
    }

    "Un equipo con un item nuevo" should "dárselo a la persona que más se beneficie de el" in {
        val item = Item(modifiers = StatBlock(Strength -> 10, Intelligence -> 20), slot = Head)
        val team = Team("", Set(mage, Hero(job = Guerrero.some)))

        val teamWithItem = team.addItem(item)

        assert(teamWithItem.members.find(_.job == Mago.some).get.equippedItems.contains(item))
        assert(!teamWithItem.members.find(_.job == Guerrero.some).get.equippedItems.contains(item))
    }
}
