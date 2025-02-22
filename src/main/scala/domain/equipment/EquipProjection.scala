package domain.equipment

import domain.Hero

/* Usado para representar cómo quedaría el stat principal de un héroe si se le equipa un item en el slot detallado
 * con un par de utilidades extra. equippedOnSlot es el slot en el que se equipó, que no es necesariamente el mismo que
 * define el ítem (no tenemos un slot para SingleHand of BothHands, pero sí tenemos manos izquierdas y derechas).
 * Como el objetivo de esa clase es ayudar con la computación del beneficio para el stat principal de equipar un item
 * se asume que ese stat principal existe, y por ende el trabajo que lo especifica
 */
case class EquipProjection(preEquipHero: Hero, postEquipHero: Hero, equippedOnSlot: ItemSlot) {
    lazy val pointsProjection: Int = postEquipHero.mainStatPoints.get
    lazy val pointsDelta: Int = pointsProjection - preEquipHero.mainStatPoints.get

    def max(other: EquipProjection): EquipProjection =
        if pointsProjection >= other.pointsProjection then this else other
}