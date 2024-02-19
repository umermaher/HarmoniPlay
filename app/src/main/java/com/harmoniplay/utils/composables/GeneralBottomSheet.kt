package com.harmoniplay.utils.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralBottomSheet(
    sheetState: SheetState,
    titleRes: Int,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        GeneralBottomSheetContent(
            titleRes = titleRes,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            content = content
        )
    }
}

@Composable
fun GeneralBottomSheetContent(
    titleRes: Int,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
        ) {
            dismissButton?.invoke()
        }

        Text(
            text = stringResource(id = titleRes),
//            style = MaterialTheme.typography.titleLarge.copy(color = textColor),
            modifier = Modifier
                .align(Alignment.Center),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
        ) {
            confirmButton?.invoke()
        }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        content()
    }
    Spacer(modifier = Modifier.height(44.dp))
}