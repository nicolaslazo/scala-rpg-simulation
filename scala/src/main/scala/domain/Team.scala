package domain

import scala.collection.immutable.HashMap

case class Team(name: String, earnings: Int = 0, members: Set[Hero] = Set()) {
    val bestMember: (Hero => Int) => Option[Hero] = members.maxByOption

    private def membersThatCanEquip(item: Item): Set[Hero] =
        item.equipCondition.map(condition => members.filter(condition)).getOrElse(members)

    private def membersAndMainStats(people: Set[Hero]): HashMap[Hero, Int] =
        // TODO: Entender notación _*
        HashMap(members.flatMap(hero => hero.mainStatPoints.map(hero -> _)).toSeq: _*)

    private def membersAndMainStatsWithItemEquipped(people: Set[Hero], item: Item): HashMap[Hero, (Int, ItemSlot)] = {
        item.slot match {
            case _ => HashMap(
                members
                    .map(hero => hero -> (hero.mainStatPointsWithItemEquipped(item, item.slot).get, item.slot))
                    .toSeq: _*)
        }
    }

    private def memberStatDeltas(oldStats: HashMap[Hero, Int], newStats: HashMap[Hero, (Int, ItemSlot)]): HashMap[Hero, (Int, ItemSlot)] =
        // TODO: Borrar código descartado si el otro funciona
        //        oldStats.merged(newStats) {
        //            case ((hero, valueWithoutItem), (_, valueWithItem: Int, itemSlot: ItemSlot)) =>
        //                hero -> (valueWithItem - valueWithoutItem, itemSlot)
        //        }
        newStats.collect {
            case (hero: Hero, (newStat: Int, itemSlot: ItemSlot)) if oldStats.contains(hero) => (hero, (newStat - oldStats(hero), itemSlot))
        }

    // TODO: Handlear caso de SingleHand
    def getItem(item: Item): Team = {
        val membersToCheck: Set[Hero] = membersThatCanEquip(item)
        val statsWithoutItem: HashMap[Hero, Int] = membersAndMainStats(membersToCheck)
        val statsWithItem: HashMap[Hero, (Int, ItemSlot)] = membersAndMainStatsWithItemEquipped(membersToCheck, item)
        val statDeltas: HashMap[Hero, (Int, ItemSlot)] = memberStatDeltas(statsWithoutItem, statsWithItem)
        val bestCandidate: Option[(Hero, ItemSlot)] =
            statDeltas.filter(_._2._1 > 0).maxByOption(_._2._1).map(candidate => candidate._1 -> candidate._2._2)

        bestCandidate match {
            case Some((hero, slot)) => this.copy(members = members - hero + hero.equip(item, slot).get)
            case None => this.copy(earnings = earnings + item.value)
        }
    }
}