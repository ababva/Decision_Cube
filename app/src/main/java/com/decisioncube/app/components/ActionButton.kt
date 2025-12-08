

package com.decisioncube.app.components


import androidx.compose.material3.Button


import androidx.compose.material3.Text


import androidx.compose.runtime.Composable


@Composable fun ActionButton(text: String, onClick: () -> Unit) {


Button(onClick = onClick) {
Text(text)


}


}


