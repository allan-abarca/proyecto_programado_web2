# Fase 1: Análisis de requerimientos y reglas de negocio

## Descripción del contexto

El proyecto consiste en crear una aplicacion web en donde, en nuestro caso, nos enfocaremos en una clinica privada de odontologia, donde los pacientes como el personal administrativo tendran accesos especificos para varios tipos de tareas, como cancelacion o creacion de citas, registros, o visualizacion de fechas.

## Reglas de negocio

### Usuarios

- El sistema debe permitir el registro y el inicio de sesión de usuarios con perfil de paciente o administrador.
- Cada usuario debe iniciar sesión con sus propias credenciales.
- Las funciones disponibles deben corresponder al perfil del usuario: los pacientes solicitan y cancelan sus citas; el personal administrativo gestiona las citas y consulta la agenda.
- Un paciente solo puede solicitar o cancelar sus propias citas.

### Citas

- Cada cita debe estar asociada a un paciente y tener una fecha y hora definidas.
- Una cita solo se puede crear si la fecha es válida y el horario está disponible.
- No se debe permitir más de una cita para el mismo horario disponible.
- Un paciente puede cancelar una cita que haya solicitado.
- El personal administrativo puede gestionar las citas.
- La cancelación debe actualizar la disponibilidad del horario para que pueda ser utilizado por otra cita.

### Agenda

- El personal administrativo puede consultar las citas en una agenda diaria o semanal.
- La agenda debe mostrar las citas correspondientes al día o a la semana seleccionada.
- Cada cita de la agenda debe permitir identificar al paciente, la fecha y la hora.

### Fechas y disponibilidad

- El sistema debe validar que la fecha y la hora ingresadas sean válidas antes de guardar una cita.
- No se debe crear una cita en un horario que ya esté ocupado o no disponible, de lo contrario, se informara al usuario y no registrar la cita.
- La disponibilidad debe actualizarse cuando se crea o cancela una cita.

## Supuestos adicionales

- La clínica define los días y horarios en que se pueden solicitar citas; solo esos horarios se consideran disponibles.
- Para esta versión, cada horario disponible admite una sola cita.
- Una cita se considera cancelada cuando el paciente o el personal administrativo realiza la cancelación en el sistema.
- El sistema conserva la información necesaria de las citas para que el personal pueda gestionarlas y consultarlas en la agenda.
