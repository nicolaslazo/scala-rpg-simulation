package domain.equipment

import domain.*
import domain.equipment.ItemSlot.*
import domain.stats.*
import domain.stats.Stat.*

case class Item(modifiers: StatBlock = StatBlock.empty,
                equipCondition: Option[Hero => Boolean] = None,
                effect: Option[(StatBlock, Hero) => StatBlock] = None,
                value: Int = 0,
                slot: ItemSlot) {
    def canBeEquippedBy(hero: Hero): Boolean = equipCondition.forall(_(hero))
}

object CascoVikingo extends Item(
    modifiers = StatBlock(Health -> 10),
    equipCondition = Some(_.baseAttributes.getOrElse(Strength, 0) > 30),
    value = 100,
    slot = Head
)

object PalitoMagico extends Item(
    modifiers = StatBlock(Intelligence -> 20),
    equipCondition =
        Some(heroe => heroe.job.contains(Mago) ||
            (heroe.job.contains(Ladron) && heroe.baseAttributes.getOrElse(Intelligence, 0) == 30)),
    slot = SingleHand
)

object ArcoViejo extends Item(modifiers = StatBlock(Strength -> 2), slot = BothHands)

object TalismanDeDedicacion extends Item(
    effect = Some(
        (currentStats, hero) => {
            val bonus = hero
                .mainStatPoints
                .map(_.*(.1).toInt)
                .getOrElse(0)

            currentStats.applyModifiers(StatBlock(Stat.values.map(_ -> bonus): _*))
        }
    ),
    slot = Neck)

object TalismanDelMinimalismo extends Item(
    modifiers = StatBlock(Health -> 60), // +10 que lo mencionado en consigna para considerar el talisman en sí
    effect =
        Some((currentStats, hero) => currentStats.applyModifiers(StatBlock(Health -> -10 * hero.equippedItems.size))),
    slot = Neck)

object VinchaDelBufaloDeAgua extends Item(
    equipCondition = Some(_.job.isEmpty),
    effect = Some((currentStats: StatBlock, _: Hero) =>
        if currentStats.getStat(Strength) > currentStats.getStat(Intelligence)
        then currentStats.applyModifiers(StatBlock(Intelligence -> 30))
        else currentStats.applyModifiers(StatBlock(Health -> 10, Strength -> 10, Speed -> 10))
    ),
    slot = Head
)

object TalismanMaldito extends Item(
    effect = Some((_, _) => StatBlock(Health -> 1, Strength -> 1, Speed -> 1, Intelligence -> 1)),
    slot = Neck)

object EspadaDeLaVida extends Item(
    effect = Some((currentStats, _) => currentStats.updated(Strength, currentStats.getOrElse(Health, 0))),
    slot = SingleHand)