package domain

import scala.util.Try

case class Item(modifiers: StatBlock, equipCondition: Hero => Boolean, effect: Hero => Hero, slot: ItemSlot)