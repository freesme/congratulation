package moon1it.com.confetti

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JComboBox
import javax.swing.SpinnerNumberModel

/**
 * 五彩纸屑特效的配置界面
 */
class ConfettiConfigurable : Configurable {
    private var mainPanel: JPanel? = null
    private lateinit var enabledCheckBox: JBCheckBox
    private lateinit var durationSpinner: javax.swing.JSpinner
    private lateinit var countSpinner: javax.swing.JSpinner
    private lateinit var speedSpinner: javax.swing.JSpinner
    private lateinit var gravitySpinner: javax.swing.JSpinner
    private lateinit var sizeSpinner: javax.swing.JSpinner
    private lateinit var positionComboBox: JComboBox<ConfettiPosition>
    
    override fun getDisplayName(): String = "Confetti"
    
    override fun createComponent(): JComponent {
        enabledCheckBox = JBCheckBox("启用五彩纸屑效果")
        durationSpinner = javax.swing.JSpinner(SpinnerNumberModel(3000, 1000, 10000, 500))
        countSpinner = javax.swing.JSpinner(SpinnerNumberModel(150, 50, 500, 10))
        speedSpinner = javax.swing.JSpinner(SpinnerNumberModel(1.0, 0.5, 3.0, 0.1))
        gravitySpinner = javax.swing.JSpinner(SpinnerNumberModel(0.2, 0.1, 1.0, 0.1))
        sizeSpinner = javax.swing.JSpinner(SpinnerNumberModel(1.0, 0.5, 2.0, 0.1))
        positionComboBox = JComboBox(ConfettiPosition.values())
        
        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckBox)
            .addLabeledComponent("动画持续时间(毫秒):", durationSpinner)
            .addLabeledComponent("五彩纸屑数量:", countSpinner)
            .addLabeledComponent("速度倍数:", speedSpinner)
            .addLabeledComponent("重力效果:", gravitySpinner)
            .addLabeledComponent("大小倍数:", sizeSpinner)
            .addLabeledComponent("显示位置:", positionComboBox)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        return mainPanel!!
    }
    
    override fun isModified(): Boolean {
        val settings = ConfettiSettings.getInstance()
        return enabledCheckBox.isSelected != settings.enabled ||
               durationSpinner.value != settings.duration ||
               countSpinner.value != settings.count ||
               speedSpinner.value != settings.speed ||
               gravitySpinner.value != settings.gravity ||
               sizeSpinner.value != settings.size ||
               positionComboBox.selectedItem != settings.position
    }
    
    override fun apply() {
        val settings = ConfettiSettings.getInstance()
        settings.enabled = enabledCheckBox.isSelected
        settings.duration = durationSpinner.value as Int
        settings.count = countSpinner.value as Int
        settings.speed = (speedSpinner.value as Double).toFloat()
        settings.gravity = (gravitySpinner.value as Double).toFloat()
        settings.size = (sizeSpinner.value as Double).toFloat()
        settings.position = positionComboBox.selectedItem as ConfettiPosition
    }
    
    override fun reset() {
        val settings = ConfettiSettings.getInstance()
        enabledCheckBox.isSelected = settings.enabled
        durationSpinner.value = settings.duration
        countSpinner.value = settings.count
        speedSpinner.value = settings.speed.toDouble()
        gravitySpinner.value = settings.gravity.toDouble()
        sizeSpinner.value = settings.size.toDouble()
        positionComboBox.selectedItem = settings.position
    }
    
    override fun disposeUIResources() {
        mainPanel = null
    }
}
