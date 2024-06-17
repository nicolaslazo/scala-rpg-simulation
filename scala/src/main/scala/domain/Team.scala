package domain

import domain.equipment.ItemSlot.{LeftHand, RightHand, SingleHand}
import domain.equipment.{EquipProjection, Item, ItemSlot}

import scala.collection.immutable.HashMap

case class Team(name: String, members: Set[Hero] = Set(), earnings: Int = 0) {
    val bestMember: (Hero => Int) => Option[Hero] = members.maxByOption

    private def membersThatCanEquip(item: Item): Set[Hero] =
        item.equipCondition.map(condition => members.filter(condition)).getOrElse(members)

    private def memberEquipProjections(people: Set[Hero],
                                       item: Item,
                                       targetSlot: ItemSlot): HashMap[Hero, EquipProjection] = {
        if targetSlot == SingleHand then {
            val leftHandOutcome = memberEquipProjections(people, item, LeftHand)
            val rightHandOutcome = memberEquipProjections(people, item, RightHand)

            return leftHandOutcome.merged(rightHandOutcome) {
                case ((hero, leftHandProjection), (_, rightHandProjection)) =>
                    (hero, leftHandProjection.max(rightHandProjection))
            }
        }

        // TODO: Entender notación _*
        HashMap(people.map(hero => hero -> hero.withItemEquippedProjection(item, targetSlot).get).toSeq: _*)
    }

    def getItem(item: Item): Team = {
        val membersToCheck: Set[Hero] = membersThatCanEquip(item)
        val equipProjections: HashMap[Hero, EquipProjection] = memberEquipProjections(membersToCheck, item, item.slot)
        val bestCandidate: Option[EquipProjection] =
            equipProjections.values.filter(_.pointsDelta > 0).maxByOption(_.pointsDelta)

        bestCandidate match {
            case Some(projection) => this.copy(members = members - projection.preEquipHero + projection.postEquipHero)
            case None => this.copy(earnings = earnings + item.value)
        }
    }
}