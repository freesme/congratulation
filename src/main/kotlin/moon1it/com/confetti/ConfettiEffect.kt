package moon1it.com.confetti

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import java.awt.*
import java.awt.event.ActionListener
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

class ConfettiEffect {
    companion object {
        fun show(project: Project) {
            val settings = ConfettiSettings.getInstance()
            if (!settings.enabled) return

            SwingUtilities.invokeLater {
                val frame = JFrame()
                frame.isUndecorated = true
                frame.background = Color(0, 0, 0, 0)
                // 根据设置的位置计算窗口位置
                val screenSize = frame.toolkit.screenSize
                frame.size = Dimension(screenSize.width, screenSize.height)

                val windowManager = WindowManager.getInstance()
                val ideFrame = windowManager.getFrame(project)

                when (settings.position) {
                    ConfettiPosition.CENTER -> {
                        frame.location = Point(0, 0)
                    }

                    ConfettiPosition.TOP -> {
                        frame.location = Point(
                            screenSize.width / 4,
                            0
                        )
                    }

                    ConfettiPosition.BOTTOM -> {
                        frame.location = Point(
                            (screenSize.width - frame.width) / 4,
                            screenSize.height - frame.height
                        )
                    }

                    ConfettiPosition.CURSOR -> {
                        val editor = EditorFactory.getInstance().allEditors.firstOrNull()
                        if (editor != null) {
                            val caretModel = editor.caretModel
                            val visualPosition = caretModel.visualPosition
                            val point = editor.visualPositionToXY(visualPosition)
                            frame.location = Point(
                                point.x,
                                point.y
                            )
                        } else {
                            // 如果找不到编辑器，默认显示在中央
                            frame.location = Point(
                                screenSize.width / 2,
                                screenSize.height / 4
                            )
                        }
                    }
                }

                val confettiPanel = ConfettiPanel(settings)
                frame.add(confettiPanel)
                frame.isVisible = true

                // 使用 Timer 自动关闭窗口，性能更好
                Timer(settings.duration, ActionListener {
                    frame.dispose()
                }).apply {
                    isRepeats = false
                    start()
                }
            }
        }
    }
}

class ConfettiPanel(private val settings: ConfettiSettings) : JPanel() {
    private val confetti = mutableListOf<Confetti>()
    private var animationTimer: Timer? = null
    private var time = 0.0
    private val random = Random.Default

    init {
        isOpaque = false
        // 使用 SwingUtilities.invokeLater 确保组件完全初始化后再开始
        SwingUtilities.invokeLater {
            startConfetti()
        }
    }

    fun startConfetti() {
        // 延迟获取窗口尺寸，确保窗口已完全初始化
        SwingUtilities.invokeLater {
            val centerX = if (width > 0) width / 2.0 else 400.0 // 默认值作为后备
            val centerY = if (height > 0) height / 2.0 else 300.0 // 修改为真正的中心位置

            // 创建不同形状的五彩纸屑，分布更均匀
            val totalCount = settings.count
            val shapesCount = ConfettiShape.values().size

            ConfettiShape.values().forEachIndexed { index, shape ->
                val countForShape = totalCount / shapesCount + if (index < totalCount % shapesCount) 1 else 0
                repeat(countForShape) {
                    // 在中心点周围添加一些随机偏移，让起始位置更自然
                    val offsetX = centerX + random.nextDouble(-30.0, 30.0)
                    val offsetY = centerY + random.nextDouble(-20.0, 20.0)
                    confetti.add(Confetti(offsetX, offsetY, shape, settings, random))
                }
            }

            // 确保在创建完粒子后再启动动画
            startAnimation()
        }
    }

    private fun startAnimation() {
        // 使用 Swing Timer 代替 Thread，性能更好，避免线程同步问题
        animationTimer = Timer(16) { // 约60FPS
            time += 0.016
            updateConfetti()
            repaint()
        }
        animationTimer?.start()
    }

    private fun updateConfetti() {
        // 批量更新和清理，减少迭代次数
        val iterator = confetti.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.update(time)

            // 更严格的边界检查，提前清理不可见粒子
            if (particle.y > height + 100 ||
                particle.x < -100 ||
                particle.x > width + 100 ||
                particle.alpha <= 0.01
            ) {
                iterator.remove()
            }
        }

        // 当所有五彩纸屑都消失时，停止动画
        if (confetti.isEmpty()) {
            animationTimer?.stop()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        // 优化渲染质量
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

        // 批量绘制，减少状态切换
        confetti.forEach { it.draw(g2d) }
    }

    // 清理资源
    override fun removeNotify() {
        super.removeNotify()
        animationTimer?.stop()
    }
}

enum class ConfettiShape {
    RECTANGLE, CIRCLE, TRIANGLE, STAR, HEART
}

class Confetti(
    private val startX: Double,
    private val startY: Double,
    private val shape: ConfettiShape,
    private val settings: ConfettiSettings,
    private val random: Random
) {
    var x: Double = startX
    var y: Double = startY

    // 缓存计算结果，避免重复计算
    private val baseWidth: Double = random.nextDouble(6.0, 12.0) * settings.size
    private val baseHeight: Double = random.nextDouble(4.0, 8.0) * settings.size
    private val speed: Double = random.nextDouble(5.0, 10.0) * settings.speed
    private val angle: Double = random.nextDouble(0.0, 360.0)
    private val rotationSpeed: Double = random.nextDouble(-15.0, 15.0)
    private var currentRotation: Double = random.nextDouble(0.0, 360.0)

    // 增强的颜色系统，支持更丰富的颜色
    private val baseColor: Color = generateRandomColor()
    private val colorVariation: Double = random.nextDouble(0.8, 1.2)

    // 改进的物理系统
    private var gravity: Double = settings.gravity.toDouble()
    private var velocityX: Double = cos(Math.toRadians(angle)) * speed
    private var velocityY: Double = sin(Math.toRadians(angle)) * speed
    private val airResistance: Double = 0.999 // 空气阻力
    private val bounceDecay: Double = 0.7 // 弹跳衰减

    // 更自然的摆动效果
    private var wobbleOffset: Double = random.nextDouble(0.0, 2 * PI)
    private var wobbleSpeed: Double = random.nextDouble(2.0, 5.0)
    private var wobbleAmount: Double = random.nextDouble(0.5, 2.0)

    // 添加生命周期和淡出效果
    private val maxLife: Double = random.nextDouble(3.0, 6.0)
    private var life: Double = 0.0
    var alpha: Double = 1.0
        private set

    // 添加缩放动画
    private var scale: Double = 1.0
    private val scaleSpeed: Double = random.nextDouble(0.5, 1.5)

    // 添加闪烁效果
    private val sparkleSpeed: Double = random.nextDouble(2.0, 4.0)
    private var sparkleOffset: Double = random.nextDouble(0.0, 2 * PI)

    private fun generateRandomColor(): Color {
        // 生成更鲜艳和有趣的颜色
        val colorSchemes = arrayOf(
            // 彩虹色
            arrayOf(
                Color(255, 0, 0),
                Color(255, 165, 0),
                Color(255, 255, 0),
                Color(0, 255, 0),
                Color(0, 0, 255),
                Color(75, 0, 130),
                Color(238, 130, 238)
            ),
            // 金属色
            arrayOf(Color(255, 215, 0), Color(192, 192, 192), Color(205, 127, 50), Color(255, 20, 147)),
            // 霓虹色
            arrayOf(Color(57, 255, 20), Color(255, 20, 147), Color(0, 191, 255), Color(255, 105, 180))
        )

        val scheme = colorSchemes[random.nextInt(colorSchemes.size)]
        return scheme[random.nextInt(scheme.size)]
    }

    fun update(time: Double) {
        life += 0.016

        // 更新位置
        x += velocityX
        y += velocityY

        // 改进的重力和空气阻力
        velocityY += gravity
        velocityX *= airResistance
        velocityY *= airResistance

        // 更自然的摆动效果
        val wobbleX = sin(time * wobbleSpeed + wobbleOffset) * wobbleAmount
        val wobbleY = cos(time * wobbleSpeed * 0.7 + wobbleOffset) * wobbleAmount * 0.5
        velocityX += wobbleX * 0.1
        velocityY += wobbleY * 0.05

        // 更新旋转
        currentRotation += rotationSpeed

        // 缩放动画
        scale = 1.0 + sin(time * scaleSpeed) * 0.2

        // 生命周期和透明度
        if (life > maxLife * 0.6) {
            alpha = max(0.0, 1.0 - (life - maxLife * 0.6) / (maxLife * 0.4))
        }

        // 添加一些随机扰动，但减少频率
        if (random.nextDouble() < 0.05) {
            velocityX += random.nextDouble(-0.3, 0.3)
            velocityY += random.nextDouble(-0.2, 0.2)
        }
    }

    fun draw(g: Graphics2D) {
        if (alpha <= 0.01) return

        // 计算当前颜色，添加闪烁效果
        val sparkle = (sin(System.currentTimeMillis() * 0.01 * sparkleSpeed + sparkleOffset) + 1) * 0.5
        val brightness = (0.7 + sparkle * 0.3) * colorVariation

        val currentColor = Color(
            (baseColor.red * brightness).toInt().coerceIn(0, 255),
            (baseColor.green * brightness).toInt().coerceIn(0, 255),
            (baseColor.blue * brightness).toInt().coerceIn(0, 255),
            (200 * alpha).toInt().coerceIn(0, 255)
        )

        g.color = currentColor

        // 保存当前变换状态
        val originalTransform = g.transform

        // 应用变换
        g.translate(x, y)
        g.rotate(Math.toRadians(currentRotation))
        g.scale(scale, scale)

        val width = (baseWidth * scale).toInt()
        val height = (baseHeight * scale).toInt()

        when (shape) {
            ConfettiShape.RECTANGLE -> {
                g.fillRect(-width / 2, -height / 2, width, height)
            }

            ConfettiShape.CIRCLE -> {
                g.fillOval(-width / 2, -width / 2, width, width)
            }

            ConfettiShape.TRIANGLE -> {
                val xPoints = intArrayOf(0, -width / 2, width / 2)
                val yPoints = intArrayOf(-height / 2, height / 2, height / 2)
                g.fillPolygon(xPoints, yPoints, 3)
            }

            ConfettiShape.STAR -> {
                drawStar(g, width)
            }

            ConfettiShape.HEART -> {
                drawHeart(g, width)
            }
        }

        // 恢复变换状态
        g.transform = originalTransform
    }

    private fun drawStar(g: Graphics2D, size: Int) {
        val outerRadius = size / 2
        val innerRadius = outerRadius / 2
        val nPoints = 5

        val xPoints = IntArray(nPoints * 2)
        val yPoints = IntArray(nPoints * 2)

        for (i in 0 until nPoints * 2) {
            val angle = i * PI / nPoints
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            xPoints[i] = (cos(angle) * radius).toInt()
            yPoints[i] = (sin(angle) * radius).toInt()
        }

        g.fillPolygon(xPoints, yPoints, nPoints * 2)
    }

    private fun drawHeart(g: Graphics2D, size: Int) {
        val scale = size / 20.0
        val path = java.awt.geom.GeneralPath()

        path.moveTo(0.0, 6.0 * scale)
        path.curveTo(-8.0 * scale, -2.0 * scale, -12.0 * scale, -8.0 * scale, -6.0 * scale, -8.0 * scale)
        path.curveTo(-3.0 * scale, -8.0 * scale, 0.0, -5.0 * scale, 0.0, -2.0 * scale)
        path.curveTo(0.0, -5.0 * scale, 3.0 * scale, -8.0 * scale, 6.0 * scale, -8.0 * scale)
        path.curveTo(12.0 * scale, -8.0 * scale, 8.0 * scale, -2.0 * scale, 0.0, 6.0 * scale)
        path.closePath()

        g.fill(path)
    }
}