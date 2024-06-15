package domain

import cats.syntax.option.*
import domain.ItemSlot.*

import scala.collection.immutable.HashMap
import scala.util.{Failure, Success, Try}

type Equipment = HashMap[ItemSlot, Item]
type Talismans = List[Item]

case class Hero private(baseAttributes: StatBlock,
                        job: Option[Job],
                        equipment: Equipment,
                        talismans: Talismans) {
    // TODO: Handlear caso en el que se cambia de trabajo y algún equipamiento ya no se puede tener

    // Los modifiers se pueden stackear sin conocer el contexto del cálculo general
    // toSet evita que los items que ocupan las dos manos se evalúen dos veces
    private def itemModifiers(): StatBlock =
        equipment
            .values
            .toSet
            .concat(talismans)
            .foldLeft(StatBlock.empty)((stats, item) => stats.applyModifiers(item.modifiers))

    private def applyItemEffects(startingStats: StatBlock): StatBlock =
        // TODO: Aplicar al cálculo de stats
        equipment
            .values
            .toSet
            .concat(talismans)
            .flatMap(_.effect)
            .foldLeft(startingStats)((currentStats, effectToApply) => effectToApply(currentStats, this))

    private def stats(): StatBlock =
        baseAttributes
            .applyModifiers(job.map(_.modifiers).getOrElse(StatBlock.empty))
            .applyModifiers(itemModifiers())
            .map((stat, value) => (stat, value.max(1)))

    val stat: Stat => Int = stats().getOrElse(_, 1)

    // TODO: Hay alguna manera de simplificar el checkeo de cuello y ambas manos?
    def equip(what: Item, target: ItemSlot): Try[Hero] =
        if !what.equipCondition.forall(_(this))
        then Failure(CouldNotEquipException("Este heroe no cumple las condiciones para equipar esto"))
        else what.slot match {
            case BothHands if target == BothHands =>
                Success(this.copy(equipment = equipment.updated(LeftHand, what).updated(RightHand, what)))
            case SingleHand if target == LeftHand => Success(
                this.copy(equipment = equipment.filterNot((_, item) => item.slot == BothHands).updated(LeftHand, what)))
            case SingleHand if target == RightHand => Success(
                this.copy(equipment = equipment.filterNot((_, item) => item.slot == BothHands).updated(RightHand, what)))
            case Neck if target == Neck => Success(this.copy(talismans = what :: talismans))
            case other if other == target => Success(this.copy(equipment = equipment.updated(target, what)))
            case _ => Failure(CouldNotEquipException("No se puede equipar este item en este slot"))
        }

    val equippedItems: Set[Item] = equipment.values.toSet.concat(talismans)

    // TODO: Es janky ese unwrapping y wrapping la línea siguiente?
    val mainStat: Option[(Stat, Int)] = for {
        unwrappedJob <- job
        jobMainStat <- unwrappedJob.mainStat.some
        points = stat(jobMainStat)
    } yield (jobMainStat, points)
}

object Hero {
    def apply(baseAttributes: StatBlock = StatBlock.empty, job: Option[Job] = None): Hero =
        Hero(baseAttributes, job, equipment = new Equipment(), talismans = List())
}