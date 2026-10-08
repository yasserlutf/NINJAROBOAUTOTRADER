package nerd.forex.ninjaroboautotrader.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LoginBg = Color(0xFF080E18)
private val LoginSurface = Color(0xFF121D2D)
private val LoginOutline = Color(0xFF405878)
private val LoginText = Color(0xFFEFF4FC)
private val LoginMuted = Color(0xFFA5B3C8)
private val LoginTeal = Color(0xFF63E1C3)
private val LoginGold = Color(0xFFFFC981)

@Composable
fun NinjaRoboLoginBrand() {
    NinjaRoboBrandLockup(compact = false)
}

@Composable
fun LoginScreen(
    onGoogleSignInClicked: () -> Unit
) {
    LoginScreen(
        onGoogleSignIn = onGoogleSignInClicked,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LoginScreen(
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0D1B25), LoginBg, Color(0xFF101327))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val isTablet = maxWidth >= 720.dp
        if (isTablet) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp)
                    .padding(horizontal = 40.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoginIntro(Modifier.weight(1f))
                LoginCard(onGoogleSignIn, Modifier.weight(0.9f))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoginCard(onGoogleSignIn, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LoginIntro(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "TRADING CONTROL · MADE CLEAR",
            color = LoginTeal,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            "Stay in control of every trade.",
            color = LoginText,
            fontSize = 42.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.5).sp
        )
        Text(
            "Monitor your automated strategy, account health, and risk controls from one focused dashboard.",
            color = LoginMuted,
            fontSize = 14.sp,
            lineHeight = 23.sp
        )
    }
}

@Composable
private fun LoginCard(
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 430.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LoginSurface),
        border = BorderStroke(1.5.dp, LoginOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF17263A), LoginSurface)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedLoginBrand()
            HorizontalDivider(color = LoginOutline.copy(alpha = 0.75f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("Welcome back", color = LoginText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Sign in securely to access your trading dashboard, sync your MT5 telemetry, and manage your risk settings.",
                    color = LoginMuted,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            }
            Button(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF4F7FB),
                    contentColor = Color(0xFF141D2A)
                )
            ) {
                Text("G", color = Color(0xFF4285F4), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(10.dp))
                Text("Continue with Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFF101E2B))
                    .border(1.dp, Color(0xFF2D4355), RoundedCornerShape(11.dp))
                    .padding(11.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("▣", color = LoginTeal, fontSize = 13.sp)
                Text(
                    "Sign-in is handled by Google. Ninja Robo Forex does not ask for your Google password.",
                    color = LoginMuted,
                    fontSize = 9.sp,
                    lineHeight = 14.sp
                )
            }
            Text(
                "Secure access to your Ninja Robo Forex dashboard.",
                color = Color(0xFF8292A9),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnimatedLoginBrand() {
    val transition = rememberInfiniteTransition(label = "Ninja Robo logo")
    val haloScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo halo scale"
    )
    val haloAlpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo halo alpha"
    )
    val logoOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo float"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(108.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(94.dp)
                    .graphicsLayer {
                        scaleX = haloScale
                        scaleY = haloScale
                        alpha = haloAlpha
                    }
                    .border(1.dp, LoginTeal.copy(alpha = haloAlpha), CircleShape)
            )
            LogoMark(
                Modifier
                    .size(78.dp)
                    .offset(y = logoOffset.dp)
            )
        }
        Text(
            "NINJA ROBO FOREX",
            color = LoginTeal,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        Text(
            "AUTOMATION WITH DISCIPLINED RISK",
            color = LoginMuted,
            fontSize = 7.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(23.dp))
            .background(LoginSurface)
            .border(1.5.dp, LoginOutline, RoundedCornerShape(23.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .padding(17.dp)
        ) {
            val n = Path().apply {
                moveTo(size.width * 0.18f, size.height * 0.82f)
                lineTo(size.width * 0.18f, size.height * 0.18f)
                lineTo(size.width * 0.80f, size.height * 0.82f)
                lineTo(size.width * 0.80f, size.height * 0.18f)
            }
            drawPath(
                path = n,
                color = LoginTeal,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            val trend = Path().apply {
                moveTo(size.width * 0.53f, size.height * 0.56f)
                lineTo(size.width * 0.66f, size.height * 0.43f)
                lineTo(size.width * 0.75f, size.height * 0.48f)
                lineTo(size.width * 0.91f, size.height * 0.28f)
            }
            drawPath(
                path = trend,
                color = LoginGold,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawLine(
                color = LoginGold,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.79f, size.height * 0.28f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.91f, size.height * 0.28f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = LoginGold,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.91f, size.height * 0.28f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.91f, size.height * 0.40f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
