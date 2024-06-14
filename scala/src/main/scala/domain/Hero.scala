package domain

import domain.ItemSlot.*

import scala.collection.immutable.HashMap
import scala.util.{Failure, Success, Try}

type StatBlock = HashMap[Stat, Int]
type Equipment = HashMap[ItemSlot, Item]
type Talismans = List[Item]

case class Hero private(baseAttributes: StatBlock,
                        job: Option[Job],
                        equipment: Equipment,
                        talismans: Talismans) {
    // TODO: Handlear caso en el que se cambia de trabajo y algún equipamiento ya no se puede tener

    /* TODO: Hay alguna manera de hacer aplicación parcial acá?
     *  En este caso en específico habría convenido tener una clase propia para agregar el método
     *  pero las ganancias de poder usar la interfaz de `HashMap` son demasiadas
     */
    private def applyModifiersToStats(stats: StatBlock, modifiers: StatBlock): StatBlock =
        stats.merged(modifiers) { case ((k, v1), (_, v2)) => (k, v1 + v2) }

    private def applyJob(startingStats: StatBlock): StatBlock =
        job.map(currentJob => applyModifiersToStats(startingStats, currentJob.modifiers)).getOrElse(startingStats)

    private def applyEquipmentModifiers(attributes: StatBlock): StatBlock =
        equipment.values.foldLeft(attributes)((stats, item) => applyModifiersToStats(stats, item.modifiers))

    private def stats(): StatBlock = {
        val jobResult = applyJob(baseAttributes)
        val equipmentResult = applyEquipmentModifiers(jobResult)
        equipmentResult.map((stat, value) => (stat, value.max(1)))
    }

    val stat: Stat => Int = stats().getOrElse(_, 1)

    // TODO: Hay alguna manera de simplificar el checkeo de cuello y ambas manos?
    def equip(what: Item, target: ItemSlot): Try[Hero] =
        if !what.equipCondition.forall(_(this)) then Failure(CouldNotEquipException("Este heroe no cumple las condiciones para equipar esto"))
        else what.slot match {
            case BothHands if target == BothHands =>
                Success(this.copy(equipment = equipment.updated(LeftHand, what).updated(RightHand, what)))
            case SingleHand if target == LeftHand => Success(this.copy(equipment = equipment.updated(LeftHand, what)))
            case SingleHand if target == RightHand => Success(this.copy(equipment = equipment.updated(RightHand, what)))
            case Neck if target == Neck => Success(this.copy(talismans = what :: talismans))
            case other if other == target => Success(this.copy(equipment = equipment.updated(target, what)))
            case _ => Failure(CouldNotEquipException("No se puede equipar este item en este slot"))
        }
}

object Hero {
    def apply(baseAttributes: StatBlock = new StatBlock(), job: Option[Job] = None): Hero =
        Hero(baseAttributes, job, equipment = new Equipment(), talismans = List())
}