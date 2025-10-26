package com.zybooks.petadoption.ui

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PetApp(
   petViewModel: PetViewModel = viewModel()
) {
   Text("To be implemented...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetAppBar(
   title: String,
   modifier: Modifier = Modifier
) {
   TopAppBar(
      title = { Text(title) },
      colors = TopAppBarDefaults.topAppBarColors(
         containerColor = MaterialTheme.colorScheme.primaryContainer
      ),
      modifier = modifier
   )
}

@Composable
fun ListScreen(
   petList: List<Pet>,
   onImageClick: (Pet) -> Unit,
   modifier: Modifier = Modifier
) {
   Scaffold(
      topBar = {
         PetAppBar(
            title = "Find a Friend"
         )
      }
   ) { innerPadding ->
      LazyVerticalGrid(
         columns = GridCells.Adaptive(minSize = 128.dp),
         contentPadding = PaddingValues(0.dp),
         modifier = modifier.padding(innerPadding)
      ) {
         items(petList) { pet ->
            Image(
               painter = painterResource(id = pet.imageId),
               contentDescription = "${pet.type} ${pet.gender}",
               modifier = Modifier.clickable(
                  onClick = { onImageClick(pet) },
                  onClickLabel = "Select the pet"
               )
            )
         }
      }
   }
}

@Preview
@Composable
fun PreviewListScreen() {
   PetAdoptionTheme {
      ListScreen(
         petList = PetDataSource().loadPets(),
         onImageClick = { }
      )
   }
}

@Composable
fun DetailScreen(
   pet: Pet,
   onAdoptClick: () -> Unit,
   modifier: Modifier = Modifier,
   onUpClick: () -> Unit = { }
) {
   val gender = if (pet.gender == PetGender.MALE) "Male" else "Female"

   Scaffold(
      topBar = {
         PetAppBar(
            title = "Details",
         )
      }
   ) { innerPadding ->
      Column(
         modifier = modifier.padding(innerPadding)
      ) {
         Image(
            painter = painterResource(pet.imageId),
            contentDescription = pet.name,
            contentScale = ContentScale.FillWidth
         )
         Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = modifier.padding(6.dp)
         ) {
            Row(
               horizontalArrangement = Arrangement.SpaceBetween,
               verticalAlignment = Alignment.CenterVertically,
               modifier = modifier.fillMaxWidth()
            ) {
               Text(
                  text = pet.name,
                  style = MaterialTheme.typography.headlineMedium
               )
               Button(onClick = onAdoptClick) {
                  Text("Adopt Me!")
               }
            }
            Text(
               text = "Gender: $gender",
               style = MaterialTheme.typography.bodyLarge
            )
            Text(
               text = "Age: ${pet.age}",
               style = MaterialTheme.typography.bodyLarge
            )
            Text(
               text = pet.description,
               style = MaterialTheme.typography.bodyMedium
            )
         }
      }
   }
}