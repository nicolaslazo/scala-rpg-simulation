package domain

import domain.equipment.ItemSlot.{LeftHand, RightHand, SingleHand}
import domain.equipment.{EquipProjection, Item, ItemSlot}

import scala.collection.immutable.HashMap

case class Team(name: String, members: Set[Hero] = Set(), earnings: Int = 0) {
    val bestMember: (Hero => Int) => Option[Hero] = members.maxByOption

    private def membersThatCanEquip(item: Item): Set[Hero] =
        item.equipCondition.map(condition => members.filter(condition)).getOrElse(members)

    private def membersAndMainStats(people: Set[Hero]): HashMap[Hero, Int] =
        // TODO: BORRAR?
        // TODO: Entender notación _*
        HashMap(members.flatMap(hero => hero.mainStatPoints.map(hero -> _)).toSeq: _*)

    private def memberEquipProjections(people: Set[Hero], item: Item): HashMap[Hero, EquipProjection] = {
        if item.slot == SingleHand then {
            val leftHandOutcome = memberEquipProjections(people, item.copy(slot = LeftHand))
            val rightHandOutcome = memberEquipProjections(people, item.copy(slot = RightHand))

            return leftHandOutcome.merged(rightHandOutcome) {
                case ((hero, leftHandProjection), (_, rightHandProjection)) =>
                    (hero, leftHandProjection.max(rightHandProjection))
            }
        }

        HashMap(people.map(hero => hero -> hero.withItemEquippedProjection(item, item.slot).get).toSeq: _*)
    }

    def getItem(item: Item): Team = {
        val membersToCheck: Set[Hero] = membersThatCanEquip(item)
        val equipProjections: HashMap[Hero, EquipProjection] = memberEquipProjections(membersToCheck, item)
        val bestCandidate: Option[EquipProjection] =
            equipProjections.values.filter(_.pointsDelta > 0).maxByOption(_.pointsDelta)

        bestCandidate match {
            case Some(projection) => this.copy(members = members - projection.preEquipHero + projection.postEquipHero)
            case None => this.copy(earnings = earnings + item.value)
        }
    }
}