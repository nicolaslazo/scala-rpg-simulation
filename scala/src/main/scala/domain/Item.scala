package domain

import domain.ItemSlot.*

case class Item(modifiers: StatBlock = StatBlock(),
                equipCondition: Option[Hero => Boolean] = None,
                effect: Option[Hero => Hero] = None,
                slot: ItemSlot)

object CascoVikingo extends Item(
    modifiers = StatBlock(health = 10),
    equipCondition = Some(_.baseAttributes.strength > 30),
    slot = Head
)

object PalitoMagico extends Item(
    modifiers = StatBlock(intelligence = 20),
    equipCondition =
        Some(heroe => heroe.job == Mago || (heroe.job == Ladron && heroe.baseAttributes.intelligence == 30)),
    slot = SingleHand
)

object ArcoViejo extends Item(modifiers = StatBlock(strength = 2), slot = BothHands)

// object TalismanDeDedicacion extends Item(effect = Some())