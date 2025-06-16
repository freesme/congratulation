package moon1it.com.confetti

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.PI
import kotlin.math.cos
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
                frame.size = Dimension(screenSize.width / 2, screenSize.height / 2)

                val windowManager = WindowManager.getInstance()
                val ideFrame = windowManager.getFrame(project)

                when (settings.position) {
                    ConfettiPosition.CENTER -> {
                        frame.location = Point(
                            screenSize.width / 10 * 3,
                            screenSize.height / 10 * 3
                        )
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
                                screenSize.width / 4,
                                screenSize.height / 5
                            )
                        }
                    }
                }

                val confettiPanel = ConfettiPanel(settings)
                frame.add(confettiPanel)
                frame.isVisible = true

                Thread {
                    Thread.sleep(settings.duration.toLong())
                    SwingUtilities.invokeLater {
                        frame.dispose()
                    }
                }.start()
            }
        }
    }
}

class ConfettiPanel(private val settings: ConfettiSettings) : JPanel() {
    private val confetti = mutableListOf<Confetti>()
    private var isRunning = false
    private var time = 0.0

    init {
        isOpaque = false
        startConfetti()
    }

    public fun startConfetti() {
        isRunning = true
        val centerX = width / 2.0
        val centerY = height / 1.5

        // 创建不同形状的五彩纸屑，只创建一次
        val countPerShape = settings.count / 3
        repeat(countPerShape) {
            confetti.add(Confetti(centerX, centerY, ConfettiShape.RECTANGLE, settings))
        }
        repeat(countPerShape) {
            confetti.add(Confetti(centerX, centerY, ConfettiShape.CIRCLE, settings))
        }
        repeat(countPerShape) {
            confetti.add(Confetti(centerX, centerY, ConfettiShape.TRIANGLE, settings))
        }

        Thread {
            while (isRunning) {
                time += 0.016 // 约60FPS
                updateConfetti()
                repaint()
                Thread.sleep(16)
            }
        }.start()
    }

    private fun updateConfetti() {
        confetti.forEach { it.update(time) }
        confetti.removeAll { it.y > height || it.x < -50 || it.x > width + 50 }

        // 当所有五彩纸屑都消失时，停止动画
        if (confetti.isEmpty()) {
            isRunning = false
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        confetti.forEach { it.draw(g2d) }
    }
}

enum class ConfettiShape {
    RECTANGLE, CIRCLE, TRIANGLE
}

class Confetti(
    private val startX: Double,
    private val startY: Double,
    private val shape: ConfettiShape,
    private val settings: ConfettiSettings
) {
    var x: Double = startX
    var y: Double = startY
    private val width: Double = Random.nextDouble(6.0, 12.0) * settings.size
    private val height: Double = Random.nextDouble(4.0, 8.0) * settings.size
    private val speed: Double = Random.nextDouble(5.0, 10.0) * settings.speed
    private val angle: Double = Random.nextDouble(0.0, 360.0)
    private val rotation: Double = Random.nextDouble(0.0, 360.0)
    private val rotationSpeed: Double = Random.nextDouble(-15.0, 15.0)
    private var currentRotation: Double = rotation
    private val color: Color = Color(
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256),
        200 // 添加一些透明度
    )
    private var gravity: Double = settings.gravity.toDouble()
    private var velocityX: Double = cos(Math.toRadians(angle)) * speed
    private var velocityY: Double = sin(Math.toRadians(angle)) * speed
    private var wobbleOffset: Double = Random.nextDouble(0.0, 2 * PI)
    private var wobbleSpeed: Double = Random.nextDouble(2.0, 5.0)
    private var wobbleAmount: Double = Random.nextDouble(0.5, 2.0)

    fun update(time: Double) {
        // 更新位置
        x += velocityX
        y += velocityY
        velocityY += gravity

        // 添加摆动效果
        velocityX += sin(time * wobbleSpeed + wobbleOffset) * wobbleAmount * 0.1

        // 更新旋转
        currentRotation += rotationSpeed

        // 添加一些随机性
        if (Random.nextDouble() < 0.1) {
            velocityX += Random.nextDouble(-0.5, 0.5)
        }
    }

    fun draw(g: Graphics2D) {
        g.color = color
        g.translate(x, y)
        g.rotate(Math.toRadians(currentRotation))

        when (shape) {
            ConfettiShape.RECTANGLE -> {
                g.fillRect(-width.toInt() / 2, -height.toInt() / 2, width.toInt(), height.toInt())
            }

            ConfettiShape.CIRCLE -> {
                g.fillOval(-width.toInt() / 2, -width.toInt() / 2, width.toInt(), width.toInt())
            }

            ConfettiShape.TRIANGLE -> {
                val xPoints = intArrayOf(0, -width.toInt() / 2, width.toInt() / 2)
                val yPoints = intArrayOf(-height.toInt() / 2, height.toInt() / 2, height.toInt() / 2)
                g.fillPolygon(xPoints, yPoints, 3)
            }
        }

        g.rotate(-Math.toRadians(currentRotation))
        g.translate(-x, -y)
    }
}