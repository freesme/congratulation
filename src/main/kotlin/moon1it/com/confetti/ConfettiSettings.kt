package moon1it.com.confetti

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 用于存储五彩纸屑特效的配置参数
 */
@State(
    name = "ConfettiSettings",
    storages = [Storage("confetti.xml")]
)
class ConfettiSettings : PersistentStateComponent<ConfettiSettings> {
    var enabled: Boolean = true
    var duration: Int = 3000
    var count: Int = 150
    var speed: Float = 1.0f
    var gravity: Float = 0.2f
    var size: Float = 1.0f
    var position: ConfettiPosition = ConfettiPosition.CENTER
    
    override fun getState(): ConfettiSettings = this
    
    override fun loadState(state: ConfettiSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    companion object {
        fun getInstance(): ConfettiSettings {
            return ApplicationManager.getApplication().service<ConfettiSettings>()
        }
    }
}

/**
 * 五彩纸屑出现位置的枚举
 */
enum class ConfettiPosition {
    CENTER,     // 屏幕中央
    TOP,        // 屏幕顶部
    BOTTOM,     // 屏幕底部
    CURSOR;     // 光标位置

    fun getDescription(): String {
        return when (this) {
            CENTER -> "屏幕中央"
            TOP -> "屏幕顶部"
            BOTTOM -> "屏幕底部"
            CURSOR -> "光标位置"
        }
    }
}
