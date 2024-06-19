package domain

import cats.syntax.option.*
import domain.equipment.ItemSlot.*
import domain.equipment.{CouldNotEquipException, EquipProjection, Item, ItemSlot}

import scala.collection.immutable.HashMap
import scala.util.{Failure, Success, Try}

type Equipment = HashMap[ItemSlot, Item]
type Talismans = List[Item]

case class Hero private(baseAttributes: StatBlock,
                        job: Option[Job],
                        equipment: Equipment,
                        talismans: Talismans) {
    // TODO: Handlear caso en el que se cambia de trabajo y algún equipamiento ya no se puede tener

    lazy val stat: Stat => Int = stats.getOrElse(_, 1)
    lazy val mainStatPoints: Option[Int] = for {
        unwrappedJob <- job
        jobMainStat <- unwrappedJob.mainStat.some
        points = stat(jobMainStat)
    } yield points
    private lazy val stats: StatBlock =
        baseAttributes
            .applyModifiers(job.map(_.modifiers).getOrElse(StatBlock.empty))
            .applyModifiers(itemModifiers)
            .applyEffects(itemEffects, this)
            .map((stat, value) => (stat, value.max(1)))
    val equippedItems: Set[Item] = equipment.values.toSet.concat(talismans)

    def withItemEquippedProjection(item: Item, slot: ItemSlot): Try[EquipProjection] =
        this.equip(item, slot).map(EquipProjection(this, _, slot))

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
            case SingleHand if target == SingleHand =>
                Failure(CouldNotEquipException("Items de SingleHand se tienen que equipar o en LeftHand o RightHand"))
            case Neck if target == Neck => Success(this.copy(talismans = what :: talismans))
            case other if other == target => Success(this.copy(equipment = equipment.updated(target, what)))
            case _ => Failure(CouldNotEquipException("No se puede equipar este item en este slot"))
        }

    def applyModifiersOnBaseAttributes(modifiers: StatBlock): Hero =
        this.copy(baseAttributes = baseAttributes.applyModifiers(modifiers))

    // TODO: Delegar el folding a StatBlock
    // Los modifiers se pueden stackear sin conocer el contexto del cálculo general
    // toSet evita que los items que ocupan las dos manos se evalúen dos veces
    private def itemModifiers: StatBlock =
        equipment
            .values
            .toSet
            .toList
            .concat(talismans)
            .map(_.modifiers)
            .foldLeft(StatBlock.empty)((one, other) => one.applyModifiers(other))

    // TODO: Aplicar al cálculo de stats
    private def itemEffects: List[(StatBlock, Hero) => StatBlock] =
        equipment.values.toSet.toList.concat(talismans).flatMap(_.effect)
}

object Hero {
    def apply(baseAttributes: StatBlock = StatBlock.empty, job: Option[Job] = None): Hero =
        Hero(baseAttributes, job, equipment = new Equipment(), talismans = List())
}