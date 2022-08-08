package it.unibo.lenziguerra.wasteservice.storage

import it.unibo.lenziguerra.wasteservice.WasteType
import it.unibo.lenziguerra.wasteservice.data.StorageStatus
import org.json.JSONObject

interface IStorageManagerSupport {
    /**
     * @param type Item type to deposit
     * @param depositAmnt Item amount to deposit
     * @return true if success, false if fail due to not enough space
     */
    fun deposit(type: String, depositAmnt: Float): Boolean

    fun getSpace(type: String): Float
    fun getAmount(type: String): Float
    fun getMax(type: String): Float

    fun getStatus(): String

    /**
     * Returns various prolog-syntax lines formatted as such:
     * `content(type, amount, maxAmount)`
     * representing the content of the StorageManager
     * @return the content lines
     */
    fun getPrologContent(): String

    // Per testing
    fun reset()
    /**
     * Takes contents in a JSON-like format
     * without surrounding {}
     * for qak compatibility
     */
    fun set(contents: String)
}

object StorageManagerSupport {
    fun getSupport(): IStorageManagerSupport {
        // if real then return real else
        // if virtual then return virtual with config path else
        return StorageManagerSupportMock()
    }
}

abstract class AbstractStorageManagerVirtual(private val maxAmount: Map<WasteType, Float>) : IStorageManagerSupport {
    /**
     * Per uso con classi virtuali/mock di storage manager, dove valori
     * massimi e tipi vengono forniti dall'alto (dove vengono caricati
     * con un metodo di configurazione a seconda)
     */
    private val amount = mutableMapOf<WasteType, Float>()

    init {
        maxAmount.keys.forEach {
            amount[it] = 0.0f
        }

        this.init()
    }

    protected abstract fun init()

    override fun getAmount(type: String): Float {
        return amount[WasteType.valueOf(type.uppercase())]!!
    }

    override fun getMax(type: String): Float {
        return maxAmount[WasteType.valueOf(type.uppercase())]!!
    }

    override fun getSpace(type: String): Float {
        return getMax(type) - getAmount(type)
    }

    // Override if needed
    protected fun preChange(type: WasteType, amount: Float) {
    }

    override fun deposit(type: String, depositAmnt: Float): Boolean {
        val typeEnum = WasteType.valueOf(type.uppercase())
        val amnt = amount[typeEnum]!!
        val maxAmnt = maxAmount[typeEnum]!!

        if (amnt + depositAmnt <= maxAmnt) {
            preChange(typeEnum, amnt + depositAmnt)
            amount[typeEnum] = amnt + depositAmnt
            return true
        }
        return false
    }

    override fun getStatus(): String {
        return "=========================\n" +
               "Storage Manager status:\n" +
                amount.map {
                    "\t${it.key.name.replaceFirstChar { c -> c.uppercase() }}: ${it.value} / ${maxAmount[it.key]}"
                }.joinToString("\n") +
               "\n========================="
    }

    override fun toString(): String = getStatus()

    override fun getPrologContent(): String {
        return amount.entries.joinToString("\n") { "content(${it.key},${it.value},${maxAmount[it.key]})" }
    }

    override fun reset() {
        amount.forEach {
            preChange(it.key, 0f)
        }
        amount.replaceAll { _, _ -> 0f }
    }

    override fun set(contents: String) {
        val jsonContents = "{$contents}"
        @Suppress("UNCHECKED_CAST")
        val contentsMap: Map<WasteType, Float> = (JSONObject(jsonContents).toMap() as Map<String, Float>)
            .mapKeys { WasteType.valueOf(it.key.uppercase()) }

        contentsMap.forEach {
            preChange(it.key, it.value)
        }
        amount.forEach {
            if (!contentsMap.containsKey(it.key)) {
                preChange(it.key, 0f)
            }
        }
        amount.clear();
        amount.putAll(contentsMap)
    }
}