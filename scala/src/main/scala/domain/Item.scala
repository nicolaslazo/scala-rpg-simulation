package domain

import cats.syntax.option.*
import domain.ItemSlot.*
import domain.Stat.*

case class Item(modifiers: StatBlock = StatBlock.empty,
                equipCondition: Option[Hero => Boolean] = None,
                effect: Option[(StatBlock, Hero) => StatBlock] = None,
                slot: ItemSlot)

object CascoVikingo extends Item(
    modifiers = StatBlock(Health -> 10),
    equipCondition = Some(_.baseAttributes.getOrElse(Strength, 0) > 30),
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
            // TODO: Hay alguna manera más idiomática de escribir esto?
//            val bonus: Int = hero.job.map(_.mainStat).map(currentStats.getStat).map(_ * .1).map(_.toInt).getOrElse(0)
            val bonus: Int = hero.mainStat.map(_._2).getOrElse(0)

            currentStats.map((stat, value) => (stat, value + bonus))
        }
    ),
    slot = Neck)
// TODO: Arreglar
//object TalismanDeDedicacion extends Item(
//    effect = Some(
//        (currentStats, hero) => {
//            // TODO: Hay alguna manera más idiomática de escribir esto?
//            //            val bonus: Int = hero.job.map(_.mainStat).map(currentStats.getStat).map(_ * .1).map(_.toInt).getOrElse(0)
////            val bonus: Int = hero.mainStatPoints.map(_._2).getOrElse(0)
//
////            currentStats.map((stat, value) => (stat, value + bonus))
//        }
//    ),
//    slot = Neck)

object TalismanDelMinimalismo extends Item(
    modifiers = StatBlock(Health -> 60), // +10 que lo mencionado en consigna para considerar el talisman en sí
    effect = Some((currentStats, hero) =>
        currentStats.updatedWith(Health)(value => Some(value.getOrElse(0).-(10 * hero.equippedItems.size)))),
    slot = Neck)

object VinchaDelBufaloDeAgua extends Item(
    equipCondition = Some(_.job.isEmpty),
    effect = Some((currentStats, _) =>
        if (for (str <- currentStats.get(Strength); int <- currentStats.get(Intelligence)) yield str > int)
            .getOrElse(false)
        then currentStats.updatedWith(Intelligence)(_.getOrElse(0).+(30).some)
        else currentStats.map((k, v) => if k == Intelligence then (k, v) else (k, v + 10))),
    slot = Head)

object TalismanMaldito extends Item(
    effect = Some((_, _) => StatBlock(Health -> 1, Strength -> 1, Speed -> 1, Intelligence -> 1)),
    slot = Neck)

object EspadaDeLaVida extends Item(
    effect = Some((currentStats, _) => currentStats.updated(Strength, currentStats.getOrElse(Health, 0))),
    slot = SingleHand)