package domain

import cats.syntax.option.*
import domain.equipment.ItemSlot.{LeftHand, RightHand, SingleHand}
import domain.equipment.{EquipProjection, Item, ItemSlot}
import domain.missions.{Task, TaskFailedException}

import scala.collection.immutable.HashMap
import scala.util.{Failure, Success, Try}

case class Team(name: String, members: Set[Hero] = Set(), earnings: Int = 0) {
    lazy val leader: Option[Hero] = {
        val maxMainStat = members.map(_.mainStatPoints).maxOption
        val leaderCandidates = maxMainStat.map(maxPoints => members.filter(_.mainStatPoints == maxPoints))

        if leaderCandidates.map(_.size).contains(1) then leaderCandidates.get.last.some else None
    }
    lazy val bestMember: (Hero => Int) => Option[Hero] = members.maxByOption

    def addItem(item: Item): Team = {
        val equipProjections: HashMap[Hero, EquipProjection] = memberEquipProjections(
            members,
            item,
            item.slot)
        val bestCandidate: Option[EquipProjection] =
            equipProjections.values.filter(_.pointsDelta > 0).maxByOption(_.pointsDelta)

        bestCandidate match {
            case Some(projection) => this.copy(members = members - projection.preEquipHero + projection.postEquipHero)
            case None => this.copy(earnings = earnings + item.value)
        }
    }

    def addMember(hero: Hero): Team = this.copy(members = members + hero)

    def getGold(amount: Int): Team = this.copy(earnings = earnings + amount)

    def perform(task: Task): Try[Team] =
        bestFor(task)
            .map(hero => this.replaceMember(hero, task.effect(hero)).get)
            .map(task.reward(_))

    def replaceMember(oldMember: Hero, newMember: Hero): Try[Team] =
        if members.contains(oldMember)
        then Success(this.copy(members = members - oldMember + newMember))
        else Failure(Exception("El héroe a remover no se encuentra en este equipo"))

    private def bestFor(task: Task): Try[Hero] =
        members
            .map(hero => hero -> task.difficultyRating(hero, this))
            .collect { case (hero, Success(diffRatingForHero)) => (hero, diffRatingForHero) }
            .maxByOption(_._2) match {
            case Some(hero, _) => Success(hero)
            case None => Failure(TaskFailedException("No se pudo encontrar un héroe que pueda hacer la tarea"))
        }

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

        HashMap(
            people.collect {
                case hero if item.canBeEquippedBy(hero) => hero -> hero.withItemEquippedProjection(item, targetSlot).get
            }.toSeq: _*)
    }
}