package moon1it.com.confetti

import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.options.Configurable

/**
 * 提供五彩纸屑特效的配置界面
 */
class ConfettiConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable {
        return ConfettiConfigurable()
    }
}
