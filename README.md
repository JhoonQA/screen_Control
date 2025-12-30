# ScreenControl

ScreenControl es una aplicación de productividad para el sistema operativo Android diseñada para el monitoreo y la gestión del tiempo de uso de aplicaciones. El objetivo principal del proyecto es proporcionar a los usuarios herramientas de bienestar digital que permitan establecer límites de tiempo diarios y analizar sus hábitos de consumo de forma local y privada.

## Características Principales

* **Monitoreo en tiempo real**: Identificación de la aplicación en primer plano y conteo preciso del tiempo de sesión.
* **Gestión de límites**: Interfaz para establecer restricciones de tiempo personalizadas por cada aplicación instalada.
* **Sistema de alertas**: Notificaciones automáticas y pantalla de bloqueo cuando el usuario alcanza el límite de tiempo configurado.
* **Reportes detallados**: Visualización del historial de uso diario y capacidad de exportación de datos en formato CSV.
* **Persistencia de datos**: Implementación de base de datos local para el almacenamiento de configuraciones y estadísticas.

## Arquitectura y Tecnologías

El proyecto ha sido desarrollado siguiendo las mejores prácticas de desarrollo en Android:

* **Lenguaje**: Kotlin.
* **Interfaz de Usuario**: Jetpack Compose para un diseño declarativo y moderno.
* **Persistencia**: Room Database para el manejo de la base de datos SQLite local.
* **Servicios**: Foreground Services para el monitoreo continuo y el procesamiento de datos.
* **Gestor de dependencias**: Gradle (Kotlin DSL).

## Requisitos y Permisos

Para cumplir con sus funciones de bienestar digital, la aplicación requiere los siguientes permisos sensibles:

1.  **PACKAGE_USAGE_STATS**: Permite acceder a las estadísticas de uso de otras aplicaciones para contabilizar el tiempo.
2.  **QUERY_ALL_PACKAGES**: Necesario para listar las aplicaciones instaladas y permitir al usuario seleccionarlas.
3.  **SYSTEM_ALERT_WINDOW**: Utilizado para mostrar la interfaz de bloqueo sobre otras aplicaciones cuando se agota el tiempo.
4.  **FOREGROUND_SERVICE**: Garantiza que el proceso de monitoreo no sea finalizado por el sistema operativo.

## Privacidad y Seguridad

ScreenControl ha sido diseñada bajo el principio de privacidad por diseño:
* No se requiere la creación de cuentas ni el registro de datos personales.
* Toda la información de uso se procesa y almacena exclusivamente en el almacenamiento local del dispositivo.
* La aplicación no cuenta con módulos de telemetría ni envío de datos a servidores externos.
