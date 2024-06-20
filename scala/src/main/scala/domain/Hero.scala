package domain

import domain.equipment.ItemSlot.*
import domain.equipment.{CouldNotEquipException, EquipProjection, Item, ItemSlot}
import domain.stats.{Stat, StatBlock, applyEffects, applyModifiers}

import scala.collection.immutable.HashMap
import scala.util.{Failure, Success, Try}

type Equipment = HashMap[ItemSlot, Item]
type Talismans = List[Item]

case class Hero private(baseAttributes: StatBlock,
                        job: Option[Job],
                        equipment: Equipment,
                        talismans: Talismans) {

    lazy val stat: Stat => Int = stats.getOrElse(_, 1)
    lazy val mainStatPoints: Option[Int] = job.map(actualJob => this.stat(actualJob.mainStat))
    private lazy val stats: StatBlock =
        baseAttributes
            .applyModifiers(job.map(_.modifiers).getOrElse(StatBlock.empty))
            .applyModifiers(itemModifiers)
            .applyEffects(itemEffects, this)
            .map((stat, value) => (stat, value.max(1)))
    private lazy val ensureEquipmentConsistency: Hero =
        this.copy(
            equipment = equipment.filter((_, item) => item.equipCondition.forall(_(this))),
            talismans = talismans.filter(_.equipCondition.forall(_(this))))
    // Los modifiers se pueden stackear sin conocer el contexto del cálculo general
    private lazy val itemModifiers: StatBlock =
        equipment
            .values
            .toSet // Para sacar duplicados de items de dos manos
            .toList
            .concat(talismans)
            .map(_.modifiers)
            .reduceOption((one, other) => one.applyModifiers(other))
            .getOrElse(StatBlock.empty)
    private lazy val itemEffects: List[(StatBlock, Hero) => StatBlock] =
        equipment.values.toSet.toList.concat(talismans).flatMap(_.effect)
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
        this.copy(baseAttributes = baseAttributes.applyModifiers(modifiers)).ensureEquipmentConsistency

    def changeJob(newJob: Option[Job]): Hero = this.copy(job = newJob).ensureEquipmentConsistency
}

object Hero {
    // El constructor customizado impide que se creen inventarios inválidos
    def apply(baseAttributes: StatBlock = StatBlock.empty, job: Option[Job] = None): Hero =
        Hero(baseAttributes, job, equipment = new Equipment(), talismans = List())
}