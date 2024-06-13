package domain

import domain.ItemSlot.*
import domain.Stat.*

import scala.collection.immutable.HashMap

case class Item(modifiers: StatBlock = HashMap(),
                equipCondition: Option[Hero => Boolean] = None,
                effect: Option[Hero => Hero] = None,
                slot: ItemSlot)

object CascoVikingo extends Item(
    modifiers = HashMap(Health -> 10),
    equipCondition = Some(_.baseAttributes.getOrElse(Strength, 0) > 30),
    slot = Head
)

object PalitoMagico extends Item(
    modifiers = HashMap(Intelligence -> 20),
    equipCondition =
        Some(heroe => heroe.job == Mago || (heroe.job == Ladron && heroe.baseAttributes.getOrElse(Intelligence, 0) == 30)),
    slot = SingleHand
)

object ArcoViejo extends Item(modifiers = HashMap(Strength -> 2), slot = BothHands)

// object TalismanDeDedicacion extends Item(effect = Some())