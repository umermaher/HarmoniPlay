package com.harmoniplay.utils.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OptionSheetCard(
    modifier: Modifier = Modifier,
    txtRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp), // Set the corner radius here
//        colors = if(!isSelected) {
//            CardDefaults.cardColors(containerColor = primaryContainerColor)
//        } else CardDefaults.cardColors(containerColor = secondaryContainerColor.copy(alpha = 0.6f))
    ) {
        Row (
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = stringResource(id = txtRes),
//                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}