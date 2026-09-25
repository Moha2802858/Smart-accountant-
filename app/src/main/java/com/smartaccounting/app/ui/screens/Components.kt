package com.smartaccounting.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartaccounting.app.ui.theme.AmberPending
import com.smartaccounting.app.ui.theme.GoldAccent
import com.smartaccounting.app.ui.theme.GreenPositive
import com.smartaccounting.app.ui.theme.NavyCard
import com.smartaccounting.app.ui.theme.NavySurface
import com.smartaccounting.app.ui.theme.RedNegative
import com.smartaccounting.app.ui.theme.TextMuted
import com.smartaccounting.app.ui.theme.TextPrimary
import com.smartaccounting.app.ui.theme.TextSecondary
import java.text.DecimalFormat

val numberFormatter = DecimalFormat("#,##0.00")

fun formatCurrency(amount: Double): String {
    return numberFormatter.format(amount)
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector? = null,
    accentColor: Color = GoldAccent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = accentColor
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, text) = when (status) {
        "posted" -> Triple(GreenPositive.copy(alpha = 0.2f), GreenPositive, "مرحل")
        "draft" -> Triple(AmberPending.copy(alpha = 0.2f), AmberPending, "مسودة")
        else -> Triple(NavyCard, TextSecondary, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AccountTypeBadge(type: String) {
    val (bgColor, textColor) = when (type) {
        "أصول" -> Pair(Color(0xFF3B82F6).copy(alpha = 0.2f), Color(0xFF60A5FA))
        "خصوم" -> Pair(Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFF87171))
        "حقوق ملكية" -> Pair(Color(0xFF8B5CF6).copy(alpha = 0.2f), Color(0xFFA78BFA))
        "إيرادات" -> Pair(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF34D399))
        "مصروفات" -> Pair(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFFBBF24))
        else -> Pair(NavyCard, TextSecondary)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = type.ifBlank { "عام" },
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionButton: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        if (actionButton != null) {
            actionButton()
        }
    }
}
