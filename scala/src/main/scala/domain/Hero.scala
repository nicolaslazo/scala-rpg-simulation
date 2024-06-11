package domain

case class Hero(baseAttributes: StatBlock, job: Option[Job]) {
    def this(baseAttributes: StatBlock) = this(baseAttributes, None)

    def health(): Int = baseAttributes.health.max(1)

    def strength(): Int = baseAttributes.strength.max(1)

    def speed(): Int = baseAttributes.speed.max(1)

    def intelligence(): Int = baseAttributes.intelligence.max(1)
}