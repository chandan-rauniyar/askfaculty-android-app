package com.chandan.askfaculty.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max

// --- Brand Tokens ---
val OrangePrimary = Color(0xFFF37021)
val OrangeDark = Color(0xFFE05E0E)
val SkyBlueAccent = Color(0xFF0284C7)
val SlateDark = Color(0xFF0F172A)
val SlateMuted = Color(0xFF64748B)
val SlateBorder = Color(0xFFE2E8F0)

val AuroraOrange = Color(0xFFF37021)
val AuroraSkyBlue = Color(0xFF0284C7)
val AuroraCyan = Color(0xFF38BDF8)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToActivate: () -> Unit = {},
    onNavigateToForgot: () -> Unit,
    onNavigateToReset: () -> Unit = {}
) {
    var regNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp

    val isSmallScreen = screenHeightDp < 680
    val topSpacer = if (isSmallScreen) 8.dp else 18.dp
    val cardSpacer = if (isSmallScreen) 16.dp else 28.dp

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF9E40).copy(alpha = 0.15f),
                        Color(0xFF38BDF8).copy(alpha = 0.10f),
                        Color(0xFFFAFBFC)
                    )
                )
            )
    ) {
        // Full-screen animated atmosphere (ported from the HTML design)
        AskFacultyAtmosphereAnimation(modifier = Modifier.fillMaxSize())

        // Scrollable Container for Compact Phones & Keyboard Visibility
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topSpacer))

            // University Pill Badge
            UniversityTopBadge()

            Spacer(modifier = Modifier.height(if (isSmallScreen) 14.dp else 22.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(if (isSmallScreen) 56.dp else 66.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = OrangePrimary.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(OrangePrimary, OrangeDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = "AskFaculty Logo",
                    tint = Color.White,
                    modifier = Modifier.size(if (isSmallScreen) 30.dp else 36.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Row {
                Text(
                    text = "Ask",
                    fontSize = if (isSmallScreen) 22.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Text(
                    text = "Faculty",
                    fontSize = if (isSmallScreen) 22.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "College communication & doubt-\nsolving workspace",
                fontSize = if (isSmallScreen) 12.sp else 13.sp,
                color = SlateMuted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(cardSpacer))

            // Form Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isSmallScreen) 16.dp else 20.dp)
                ) {
                    Text(
                        text = "Registration No. / College Email",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = regNo,
                        onValueChange = { regNo = it },
                        placeholder = {
                            Text("Enter Reg. No or student@lpu.in", fontSize = 13.sp, color = Color(0xFF94A3B8))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Badge,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SlateBorder,
                            focusedBorderColor = OrangePrimary,
                            unfocusedContainerColor = Color(0xFFFBFBFD),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )
                        TextButton(
                            onClick = onNavigateToForgot,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Forgot password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrangePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login(regNo, password)
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SlateBorder,
                            focusedBorderColor = OrangePrimary,
                            unfocusedContainerColor = Color(0xFFFBFBFD),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.login(regNo, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = OrangePrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        enabled = uiState !is LoginUiState.Loading
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Log In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- Top Badge ---
@Composable
fun UniversityTopBadge() {
    Row(
        modifier = Modifier
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, SkyBlueAccent.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = null,
            tint = SkyBlueAccent,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "LOVELY PROFESSIONAL UNIVERSITY",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SkyBlueAccent,
            letterSpacing = 0.7.sp
        )
    }
}

// ───────────────────────── Animation helpers ─────────────────────────


// ───────────────────────── Animation helpers ─────────────────────────

private fun phase(t: Long, durMs: Int, delayMs: Int = 0): Float {
    val x = (t - delayMs).toDouble() / durMs
    return (((x % 1.0) + 1.0) % 1.0).toFloat()
}

/** 0 → 1 → 0 smooth ease-in-out */
private fun pulse(p: Float): Float = (1f - cos(2f * PI.toFloat() * p)) / 2f

/** Zoom-only spec: scale range + alpha range */
private data class ZoomSpec(
    val durMs: Int,
    val s0: Float, val s1: Float,
    val a0: Float, val a1: Float
)

private val Zoom1 = ZoomSpec(7500, 0.70f, 1.35f, 0.30f, 0.85f)
private val Zoom2 = ZoomSpec(9500, 0.60f, 1.30f, 0.25f, 0.75f)
private val Zoom3 = ZoomSpec(6800, 0.75f, 1.45f, 0.40f, 0.90f)

// ───────────────────────── Main animation composable ─────────────────────────

@Composable
fun AskFacultyAtmosphereAnimation(modifier: Modifier = Modifier) {
    var timeMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) {
            withFrameMillis { timeMs = it - start }
        }
    }

    Canvas(modifier = modifier.clipToBounds()) {
        val t = timeMs
        val w = size.width
        val h = size.height
        val d = this.density // px per dp
        fun dp(v: Float) = v * d

        val orange = Color(0xFFF37021)
        val amber = Color(0xFFFF9E40)
        val blue = Color(0xFF0284C7)
        val sky = Color(0xFF38BDF8)
        val indigo = Color(0xFF6366F1)

        // ── 1. Aurora blobs: zoom in/out + brightness pulse (no movement) ──
        drawAuroraBlob(
            cx = dp(-64f) + dp(420f) / 2f, cy = dp(-64f) + dp(340f) / 2f,
            width = dp(420f), height = dp(340f),
            colors = listOf(orange.copy(0.22f), amber.copy(0.12f), amber.copy(0f)),
            p = pulse(phase(t, 11000))
        )
        drawAuroraBlob(
            cx = w + dp(80f) - dp(380f) / 2f, cy = dp(48f) + dp(360f) / 2f,
            width = dp(380f), height = dp(360f),
            colors = listOf(blue.copy(0.22f), sky.copy(0.12f), sky.copy(0f)),
            p = pulse(phase(t, 11000, -3500))
        )
        drawAuroraBlob(
            cx = dp(-80f) + dp(420f) / 2f, cy = h + dp(48f) - dp(320f) / 2f,
            width = dp(420f), height = dp(320f),
            colors = listOf(orange.copy(0.22f), amber.copy(0.12f), amber.copy(0f)),
            p = pulse(phase(t, 11000))
        )
        drawAuroraBlob(
            cx = w + dp(80f) - dp(440f) / 2f, cy = h + dp(80f) - dp(340f) / 2f,
            width = dp(440f), height = dp(340f),
            colors = listOf(blue.copy(0.22f), indigo.copy(0.10f), indigo.copy(0f)),
            p = pulse(phase(t, 11000, -5000))
        )
        // bottom-center glow, gentle breathing
        run {
            val gp = pulse(phase(t, 9000))
            val c = Offset(w / 2f, h - dp(24f) - dp(160f) / 2f)
            val r = max(dp(340f), dp(160f)) / 2f * lerp(0.9f, 1.15f, gp)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(orange.copy(0.10f), sky.copy(0.07f), sky.copy(0f)),
                    center = c, radius = r
                ),
                radius = r, center = c,
                alpha = lerp(0.7f, 1f, gp)
            )
        }

        // ── 2. Waves: scale only, pivot at the bottom (grows upward like a swell) ──
        val bottomPivot = Offset(w / 2f, h)

        // Back wave
        run {
            val layerH = dp(270f)
            val top = h - layerH
            val p = pulse(phase(t, 13000))
            val path = buildWavePath(
                w, top, layerH, 340f, 170f,
                floatArrayOf(240f, 240f, 460f, 110f, 720f, 180f, 980f, 250f, 1200f, 120f, 1440f, 165f)
            )
            val brush = Brush.linearGradient(
                0f to sky.copy(0.14f),
                0.45f to Color(0xFF818CF8).copy(0.09f),
                0.85f to orange.copy(0.16f),
                start = Offset(0f, top), end = Offset(w, top + layerH)
            )
            withTransform({
                scale(lerp(1.00f, 1.10f, p), lerp(1.00f, 1.18f, p), pivot = bottomPivot)
            }) { drawPath(path, brush, alpha = lerp(0.60f, 0.85f, p)) }
        }
        // Mid wave
        run {
            val layerH = dp(230f)
            val top = h - layerH
            val p = pulse(phase(t, 10000))
            val path = buildWavePath(
                w, top, layerH, 300f, 130f,
                floatArrayOf(200f, 80f, 440f, 210f, 720f, 140f, 1000f, 70f, 1220f, 190f, 1440f, 120f)
            )
            val brush = Brush.linearGradient(
                0f to amber.copy(0.22f),
                0.55f to orange.copy(0.18f),
                1f to blue.copy(0.12f),
                start = Offset(w, top), end = Offset(0f, top + layerH)
            )
            withTransform({
                scale(lerp(1.00f, 1.08f, p), lerp(0.92f, 1.15f, p), pivot = bottomPivot)
            }) { drawPath(path, brush, alpha = lerp(0.70f, 0.95f, p)) }
        }
        // Front wave + glowing top edge
        run {
            val layerH = dp(180f)
            val top = h - layerH
            val p = pulse(phase(t, 8000))
            val path = buildWavePath(
                w, top, layerH, 240f, 100f,
                floatArrayOf(320f, 170f, 540f, 60f, 820f, 115f, 1100f, 170f, 1300f, 75f, 1440f, 90f)
            )
            val fill = Brush.linearGradient(
                0f to orange.copy(0.22f),
                0.5f to Color(0xFFFFB169).copy(0.16f),
                1f to sky.copy(0.15f),
                start = Offset(0f, top), end = Offset(w, top + layerH * 0.8f)
            )
            val sx = w / 1440f
            val syy = layerH / 240f
            val edge = Path().apply {
                moveTo(0f, top + 100f * syy)
                cubicTo(320f * sx, top + 170f * syy, 540f * sx, top + 60f * syy, 820f * sx, top + 115f * syy)
                cubicTo(1100f * sx, top + 170f * syy, 1300f * sx, top + 75f * syy, 1440f * sx, top + 90f * syy)
            }
            val strokeBrush = Brush.horizontalGradient(
                listOf(orange.copy(0.6f), Color.White.copy(0.7f), blue.copy(0.5f)),
                startX = 0f, endX = w
            )
            withTransform({
                scale(lerp(1.00f, 1.06f, p), lerp(0.95f, 1.22f, p), pivot = bottomPivot)
            }) {
                drawPath(path, fill, alpha = lerp(0.75f, 1f, p))
                drawPath(edge, strokeBrush, alpha = 0.4f * lerp(0.75f, 1f, p), style = Stroke(width = dp(2f)))
            }
        }

        // ── 3. Constellation net: fixed position, zoom + fade ──
        run {
            val cw = dp(176f)
            val ch = dp(144f)
            val cl = w - dp(24f) - cw
            val ct = h - dp(24f) - ch
            val p = pulse(phase(t, 12000))
            val a = lerp(0.40f, 0.85f, p)
            val px = cw / 180f
            val py = ch / 150f
            fun pt(x: Float, y: Float) = Offset(cl + x * px, ct + y * py)
            val dash = PathEffect.dashPathEffect(floatArrayOf(2f * px, 3f * px))
            val lw = dp(1f)
            val z = lerp(0.88f, 1.18f, p)

            withTransform({
                scale(z, z, pivot = Offset(cl + cw / 2f, ct + ch / 2f))
            }) {
                fun line(a1: Offset, a2: Offset, c: Color, al: Float) =
                    drawLine(c.copy(alpha = al * a), a1, a2, strokeWidth = lw, pathEffect = dash)
                line(pt(30f, 95f), pt(85f, 45f), blue, 0.45f)
                line(pt(85f, 45f), pt(145f, 75f), orange, 0.55f)
                line(pt(145f, 75f), pt(160f, 120f), orange, 0.35f)
                line(pt(85f, 45f), pt(70f, 120f), blue, 0.35f)

                drawCircle(blue.copy(0.70f * a), 2.5f * px, pt(30f, 95f))
                drawCircle(orange.copy(0.90f * a), 3.5f * px, pt(85f, 45f))
                drawCircle(sky.copy(0.85f * a), 3f * px, pt(145f, 75f))
                drawCircle(orange.copy(0.65f * a), 2f * px, pt(160f, 120f))
                drawCircle(blue.copy(0.50f * a), 2f * px, pt(70f, 120f))
            }
        }

        // ── 4. Drifters: stay in place, zoom in/out + fade ──
        fun leftX(pct: Float, sizeDp: Float) = w * pct + dp(sizeDp) / 2f
        fun rightX(pct: Float, sizeDp: Float) = w - w * pct - dp(sizeDp) / 2f
        fun bottomY(bottomDp: Float, sizeDp: Float) = h - dp(bottomDp) - dp(sizeDp) / 2f

        drawZoomDot(Offset(leftX(0.14f, 12f), bottomY(112f, 12f)), dp(12f), orange, Color(0xFFFFB169), Zoom1, t, 0)
        drawZoomDot(Offset(leftX(0.28f, 8f), bottomY(64f, 8f)), dp(8f), blue.copy(0.70f), blue.copy(0.70f), Zoom2, t, 0)
        drawZoomDot(Offset(leftX(0.46f, 10f), bottomY(144f, 10f)), dp(10f), amber.copy(0.75f), amber.copy(0.75f), Zoom3, t, 0)
        drawZoomDot(Offset(rightX(0.22f, 14f), bottomY(80f, 14f)), dp(14f), sky, blue, Zoom1, t, -3000)
        drawZoomDot(Offset(rightX(0.12f, 8f), bottomY(160f, 8f)), dp(8f), orange.copy(0.80f), orange.copy(0.80f), Zoom2, t, -4500)
        drawZoomDot(Offset(rightX(0.42f, 6f), bottomY(48f, 6f)), dp(6f), Color(0xFF818CF8).copy(0.70f), Color(0xFF818CF8).copy(0.70f), Zoom3, t, -2000)

        // ── 5. Sparkles: fixed position, twinkle by zoom + fade ──
        drawZoomSparkle(Offset(leftX(0.22f, 12f), bottomY(96f, 12f)), dp(12f), orange, 5000, 0, t)
        drawZoomSparkle(Offset(rightX(0.28f, 14f), bottomY(128f, 14f)), dp(14f), blue, 6500, 1800, t)
        drawZoomSparkle(Offset(leftX(0.58f, 10f), bottomY(56f, 10f)), dp(10f), amber, 4800, 3200, t)
    }
}

// ───────────────────────── Draw helpers ─────────────────────────

private fun DrawScope.drawAuroraBlob(
    cx: Float, cy: Float, width: Float, height: Float,
    colors: List<Color>, p: Float
) {
    val center = Offset(cx, cy) // fixed center, no movement
    val radius = max(width, height) / 2f * 1.1f * lerp(0.88f, 1.25f, p)
    drawCircle(
        brush = Brush.radialGradient(colors, center = center, radius = radius),
        radius = radius,
        center = center,
        alpha = lerp(0.65f, 1f, p)
    )
}

private fun buildWavePath(
    w: Float, top: Float, layerH: Float, viewH: Float,
    startY: Float, c: FloatArray
): Path {
    val sx = w / 1440f
    val sy = layerH / viewH
    fun fx(v: Float) = v * sx
    fun fy(v: Float) = top + v * sy
    return Path().apply {
        moveTo(0f, fy(startY))
        cubicTo(fx(c[0]), fy(c[1]), fx(c[2]), fy(c[3]), fx(c[4]), fy(c[5]))
        cubicTo(fx(c[6]), fy(c[7]), fx(c[8]), fy(c[9]), fx(c[10]), fy(c[11]))
        lineTo(w, top + layerH)
        lineTo(0f, top + layerH)
        close()
    }
}

private fun DrawScope.drawZoomDot(
    center: Offset, sizePx: Float,
    c1: Color, c2: Color,
    spec: ZoomSpec, t: Long, delayMs: Int
) {
    val p = pulse(phase(t, spec.durMs, delayMs))
    val radius = sizePx / 2f * lerp(spec.s0, spec.s1, p)
    val a = lerp(spec.a0, spec.a1, p)

    // soft glow grows with the dot
    drawCircle(c1.copy(alpha = c1.alpha * a * 0.25f), radius * 2.0f, center)
    drawCircle(
        brush = Brush.linearGradient(
            listOf(c1.copy(alpha = c1.alpha * a), c2.copy(alpha = c2.alpha * a)),
            start = Offset(center.x - radius, center.y + radius),
            end = Offset(center.x + radius, center.y - radius)
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawZoomSparkle(
    center: Offset, sizePx: Float, color: Color,
    durMs: Int, delayMs: Int, t: Long
) {
    val pp = pulse(phase(t, durMs, delayMs))
    val s = lerp(0.5f, 1.4f, pp)
    val a = lerp(0.25f, 0.95f, pp)

    withTransform({
        scale(s, s, pivot = center)
    }) {
        drawFourPointStar(center, sizePx / 2f, color.copy(alpha = a))
    }
}

private fun DrawScope.drawFourPointStar(center: Offset, radius: Float, color: Color) {
    val inner = radius * 0.2f
    val star = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(star, color)
}