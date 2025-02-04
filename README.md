#Flappy Chef

##🌍 Descripción
Flappy Chef es un juego para dispositivos móviles inspirado en el clásico Flappy Bird, pero con una temática única relacionada con la cocina. El objetivo es controlar a un chef volador mientras esquiva diferentes obstáculos en su camino, como torres de platos y tenedores, todo en un entorno de restaurante caótico pero divertido.

Este proyecto ha sido desarrollado utilizando Android Studio y Jetpack Compose, incorporando funcionalidades avanzadas y mecánicas de juego innovadoras para ofrecer una experiencia desafiante y entretenida.

##📚 Características Principales
###📸 Uso de la Cámara
El jugador puede tomar una foto que será utilizada como avatar personalizado en el juego.
Las imágenes capturadas se almacenan en la memoria del dispositivo y reemplazan al sprite por defecto.

###🎤 Uso del Micrófono
Mecánica innovadora que permite apagar el "fuego" en pantalla soplando al micrófono.
Apagar el fuego no solo mejora la visión del escenario, sino que también otorga puntos adicionales al jugador.

###🛠️ Uso del Acelerómetro
El movimiento del chef se controla inclinando verticalmente el dispositivo.
Sensibilidad ajustada para evitar movimientos incómodos y asegurar una experiencia fluida.

###⏳ Detector de Hora
El juego cambia su fondo de acuerdo con la hora del día, ofreciendo temas diurnos y nocturnos.
La temática del restaurante también varía en función de la hora.

##🔗 Estructura del Juego
Pantalla de Inicio: Acceso a instrucciones, configuración y juego.
Pantalla de Juego: Donde ocurre la acción principal.
Menú de Pausa: Permite reanudar, reiniciar o salir.
Pantalla Game Over: Muestra la puntuación final del jugador.

##💡 Diseño del Videojuego
Temática: Inspirada en una cocina de restaurante.
Obstáculos: Torres de platos con diferentes alturas y tenedores enemigos en movimiento horizontal.
Elementos Dinámicos: Ollas que lanzan bolas de fuego que dificultan la visión.
Power-Ups:
- Cubitos de hielo que ralentizan los elementos del escenario.
- Hadas de invencibilidad que hacen al jugador invulnerable por un tiempo limitado.
Fondo Dinámico: Cambia entre día y noche según la hora.

##🎶 Audio
Se han implementado efectos de sonido para mejorar la experiencia de juego:
Música Ambiental: Sonido continuo durante la partida.
Power-Ups: Sonidos al recoger cubitos de hielo o hadas.
Fuego: Efecto sonoro cuando el chef comienza a quemarse.
Colisiones: Sonidos al chocar con tenedores o torres de platos.

##🔧 Tecnologías Utilizadas
Lenguaje: Kotlin
Framework: Jetpack Compose
IDE: Android Studio
Sensores: Cámara, micrófono, acelerómetro

##💡 Conclusiones
El desarrollo de Flappy Chef ha permitido explorar y aplicar conocimientos avanzados sobre desarrollo de juegos móviles, integrando sensores y recursos nativos del dispositivo. A pesar de los retos técnicos encontrados, como la configuración del acelerómetro y la gestión de eventos del micrófono, el proyecto ha resultado en un juego divertido, fluido y funcional.

##📚 Instalación
Clona el repositorio:
git clone <repositorio>

Abre el proyecto en Android Studio.

Ejecuta la aplicación en un emulador o dispositivo físico.
