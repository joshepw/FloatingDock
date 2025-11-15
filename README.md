# Floating Dock

Un dock flotante personalizable para Android que te permite acceder rápidamente a tus aplicaciones favoritas y acciones del sistema desde cualquier pantalla.

<div align="center">
  <img src="docs/screenshot_1.png" alt="Screenshot 1" width="30%">
  <img src="docs/screenshot_2.png" alt="Screenshot 2" width="30%">
  <img src="docs/screenshot_3.png" alt="Screenshot 3" width="30%">
</div>

## 📥 Descarga

[![Download APK](https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android)](https://github.com/[TU_USUARIO]/[TU_REPOSITORIO]/releases/latest)

**Descarga la última versión del APK desde los [Releases](https://github.com/[TU_USUARIO]/[TU_REPOSITORIO]/releases/latest)**

## 📱 Descripción

Floating Dock es una aplicación que crea un dock flotante en tu dispositivo Android, permitiéndote acceder rápidamente a tus aplicaciones y acciones del sistema sin necesidad de volver al launcher. El dock se muestra sobre otras aplicaciones y se puede personalizar completamente según tus preferencias.

## ✨ Características

- **Dock Flotante**: Accede a tus aplicaciones favoritas desde cualquier pantalla
- **Acciones del Sistema**: Controla funciones como Home, Back, Volumen, Reproducción de medios, y más
- **Personalización Completa**:
  - Tamaño de iconos configurable
  - Posición inicial del dock (9 posiciones disponibles)
  - Color y transparencia del fondo
  - Color y transparencia de los iconos
  - Border radius del dock
  - Separación entre iconos
  - Padding de iconos
  - Márgenes desde los bordes
- **Iconos Material Symbols**: Más de 4,000 iconos disponibles
- **Iconos Nativos**: Opción de usar el icono nativo de cada aplicación
- **Selección de Actividades**: Para apps con múltiples actividades (como launchers de auto)
- **Inicio Automático**: El servicio se inicia automáticamente al arrancar el sistema
- **Detección de Teclado**: El dock se oculta automáticamente cuando se abre el teclado

## 🚀 Instalación

1. Descarga el archivo APK desde la sección [Descarga](#-descarga) arriba o desde los [Releases](https://github.com/[TU_USUARIO]/[TU_REPOSITORIO]/releases/latest)
2. Habilita la instalación desde fuentes desconocidas en tu dispositivo Android
3. Instala el APK
4. Abre la aplicación y otorga los permisos necesarios:
   - **Permiso de superposición**: Necesario para mostrar el dock sobre otras aplicaciones
   - **Permiso de inicio automático**: Para que el servicio se inicie al arrancar el dispositivo

## 📖 Cómo Usar

### Configuración Inicial

1. **Permisos**: Al abrir la app por primera vez, se te solicitará el permiso de superposición. Ve a Configuración y habilita "Mostrar sobre otras aplicaciones" para Floating Dock.

2. **Agregar Aplicaciones al Dock**:
   - Toca el botón "Agregar app al dock"
   - Selecciona una aplicación de la lista
   - Si la app tiene múltiples actividades, selecciona la que deseas usar
   - Elige un icono Material Symbol o usa el icono nativo de la app
   - El dock se actualizará automáticamente

3. **Agregar Acciones del Sistema**:
   - Toca el botón "Agregar app al dock"
   - Selecciona "Agregar acción del sistema"
   - Elige la acción que deseas (Home, Back, Volumen, etc.)
   - Selecciona un icono Material Symbol
   - La acción se agregará al dock

### Personalización del Dock

Todas las configuraciones se guardan automáticamente y se aplican inmediatamente:

- **Tamaño del Icono**: Ajusta el tamaño de los iconos en el dock (en dp)
- **Posición Inicial**: Selecciona dónde aparecerá el dock (superior izquierda, centro, inferior derecha, etc.)
- **Border Radius**: Controla qué tan redondeadas son las esquinas del dock
- **Color de Fondo**: Personaliza el color del fondo del dock
- **Transparencia de Fondo**: Ajusta la opacidad del fondo (0-255)
- **Color de Iconos**: Cambia el color de los iconos del dock
- **Transparencia de Iconos**: Ajusta la opacidad de los iconos (0-255)
- **Separación entre Iconos**: Controla el espacio entre los iconos del dock
- **Padding de Iconos**: Ajusta el espacio interno de cada icono
- **Márgenes**: Configura la distancia del dock desde los bordes de la pantalla (horizontal y vertical)

### Gestión de Aplicaciones

- **Editar**: Toca el icono de editar para cambiar el icono o la actividad de una app
- **Eliminar**: Toca el icono de eliminar para quitar una app del dock
- **Reordenar**: Las apps se muestran en el orden en que fueron agregadas

## 🎯 Casos de Uso

- **Automóviles con Android**: Acceso rápido a Radio, AC, Navegación y otras apps del sistema
- **Productividad**: Acceso rápido a aplicaciones de trabajo sin salir de la app actual
- **Gaming**: Control rápido de funciones del sistema durante el juego
- **Accesibilidad**: Facilita el acceso a aplicaciones para usuarios con dificultades de movilidad

## ⚙️ Requisitos

- Android 7.0 (Nougat) o superior
- Permiso de superposición (SYSTEM_ALERT_WINDOW)
- Permiso de inicio automático (opcional pero recomendado)

## 🔧 Solución de Problemas

### El dock no aparece
- Verifica que tengas el permiso de superposición habilitado
- Reinicia la aplicación
- Verifica que hayas agregado al menos una aplicación al dock

### El servicio no inicia automáticamente
- Algunos fabricantes requieren habilitar manualmente el inicio automático en Configuración > Aplicaciones > Floating Dock > Inicio automático
- Reinicia el dispositivo después de habilitar el permiso

### Los iconos no se muestran correctamente
- Asegúrate de que la fuente Material Symbols esté instalada correctamente
- Si un icono no aparece, intenta seleccionar otro de la lista

## 📝 Notas

- El dock se oculta automáticamente cuando se abre el teclado
- Los cambios en la configuración se aplican inmediatamente sin necesidad de guardar
- El servicio corre en segundo plano y consume recursos mínimos
- La aplicación requiere el permiso de superposición para funcionar correctamente

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Si encuentras un bug o tienes una sugerencia, por favor abre un issue en el repositorio.

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia que se especifique en el repositorio.

## 💰 Donaciones

Si este proyecto te ha sido útil y deseas apoyar su desarrollo, puedes hacer una donación usando criptomonedas:

### Bitcoin (BTC)

[![Donate Bitcoin](https://img.shields.io/badge/Donate-Bitcoin-orange?style=for-the-badge&logo=bitcoin)](bitcoin:[TU_DIRECCION_BITCOIN_AQUI])

```
bc1q9nlvf63ny8sn0wwy0jmgl0d0qtsepffqhx2uqh
```

### Ethereum (ETH)

[![Donate Ethereum](https://img.shields.io/badge/Donate-Ethereum-blue?style=for-the-badge&logo=ethereum)](ethereum:[TU_DIRECCION_ETHEREUM_AQUI])

```
0xFC9279fEe715Db4225cfbdde25595E701bfC3265
```

### USDC (USD Coin)

[![Donate USDC](https://img.shields.io/badge/Donate-USDC-blue?style=for-the-badge&logo=ethereum)](ethereum:0xFC9279fEe715Db4225cfbdde25595E701bfC3265)

```
0xFC9279fEe715Db4225cfbdde25595E701bfC3265
```

**Nota**: USDC es un token ERC-20 en la red de Ethereum. Esta dirección también puede recibir cualquier token ERC-20 (USDT, DAI, UNI, etc.) y cualquier otra red compatible con direcciones Ethereum (Polygon, BSC, Arbitrum, Optimism, etc.).

---

**Desarrollado con ❤️ para la comunidad Android**
