package domain

import org.scalatest.flatspec.AnyFlatSpec

class StatBlockTest extends AnyFlatSpec {
    "Varios stat blocks" should "ser sumados en cadena" in {
        val statBlock: StatBlock = StatBlock(1, 2, 3, 4)
        val blocks: List[StatBlock] = List(statBlock, statBlock.copy(), statBlock.copy())
        val expected: StatBlock = StatBlock(3, 6, 9, 12)

        assert(blocks.fold(StatBlock())(_ + _) === expected)
    }
}
