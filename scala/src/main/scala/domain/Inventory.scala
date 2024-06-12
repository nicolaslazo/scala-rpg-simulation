package domain

import domain.ItemSlot.*

import scala.util.{Failure, Success, Try}

case class Inventory(head: Option[Item],
                     torso: Option[Item],
                     leftHand: Option[Item],
                     rightHand: Option[Item],
                     neck: List[Item]) {
    def this() = this(None, None, None, None, List())

    // TODO: Hay manera de reducir el boilerplate?
    def equip(item: Item, hero: Hero): Try[Inventory] =
        if item.equipCondition(hero) then
            Success(item.slot match
                case Head => this.copy(head = Some(item))
                case Torso => this.copy(torso = Some(item))
                case LeftHand => this.copy(leftHand = Some(item))
                case RightHand => this.copy(rightHand = Some(item))
                case BothHands => this.copy(leftHand = Some(item), rightHand = Some(item))
                case Neck => this.copy(neck = item :: this.neck)
            ) else Failure(CouldNotEquipException("La condición de equipamiento del item no se cumple"))
}