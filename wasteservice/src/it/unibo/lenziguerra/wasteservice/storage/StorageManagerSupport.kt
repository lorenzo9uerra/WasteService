package it.unibo.lenziguerra.wasteservice.storage

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
     * Returns varios prolog-syntax lines formatted as such:
     * `content(type, amount, maxAmount)`
     * representing the content of the StorageManager
     * @return the content lines
     */
    fun getPrologContent(): String

    // Per testing
    fun reset()
    fun set(contents: Map<String, Float>)
}

object StorageManagerSupport {
    fun getSupport(): IStorageManagerSupport {
        // if real then return real else
        // if virtual then return virtual with config path else
        return StorageManagerSupportMock()
    }
}

abstract class AbstractStorageManagerVirtual(private val maxAmount: Map<String, Float>) : IStorageManagerSupport {
    /**
     * Per uso con classi virtuali/mock di storage manager, dove valori
     * massimi e tipi vengono forniti dall'alto (dove vengono caricati
     * con un metodo di configurazione a seconda)
     */
    private val amount = mutableMapOf<String, Float>();

    init {
        maxAmount.keys.forEach {
            amount[it] = 0.0f
        }

        this.init()
    }

    protected abstract fun init()

    override fun getAmount(type: String): Float {
        return amount[type]!!
    }

    override fun getMax(type: String): Float {
        return maxAmount[type]!!
    }

    override fun getSpace(type: String): Float {
        return getMax(type) - getAmount(type)
    }

    // Override if needed
    protected fun preChange(type: String, amount: Float) {
    }

    override fun deposit(type: String, depositAmnt: Float): Boolean {
        if (getAmount(type) + depositAmnt > getMax(type)) {
            preChange(type, amount[type]!! + depositAmnt)
            amount[type] = amount[type]!! + depositAmnt
            return true
        }
        return false
    }

    override fun getStatus(): String {
        return "=========================\n" +
               "Storage Manager status:\n" +
                amount.map {
                    "\t${it.key.replaceFirstChar { c -> c.uppercase() }}: ${it.value} / ${maxAmount[it.key]}"
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

    override fun set(contents: Map<String, Float>) {
        contents.forEach {
            preChange(it.key, it.value)
        }
        amount.forEach {
            if (!contents.containsKey(it.key)) {
                preChange(it.key, 0f)
            }
        }
        amount.clear();
        amount.putAll(contents)
    }
}