package domain

import scala.collection.immutable.HashMap

type StatBlock = HashMap[Stat, Int]

object StatBlock {
    def apply(): StatBlock = HashMap.empty[Stat, Int]

    // TODO: Terminar de entender esta sintaxis
    def apply(entries: (Stat, Int)*): StatBlock = HashMap(entries: _*)
    
    val empty: StatBlock = apply()
}

// TODO: realmente necesito tener un singleton object Y métodos de extensión para conseguir todo lo que necesito?
extension (statBlock: StatBlock) {
    def getStat(stat: Stat): Int = statBlock.getOrElse(stat, 0)

    def applyModifiers(modifiers: StatBlock): StatBlock =
        statBlock.merged(modifiers) { case ((k, v1), (_, v2)) => (k, v1 + v2) }
}