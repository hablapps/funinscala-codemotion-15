# Elimina la corrupción: programación funcional pura con Scala

_ATENCIÓN: Este workshop estaba inicialmente programado para el viernes 27. La nueva fecha es el Sábado 28 a las 15:30. Disculpad las molestias._

### Descripción

[Workshop Codemotion 2015](http://2015.codemotion.es/agenda.html#5699289732874240/49554011)

### Instrucciones

Para seguir el workshop únicamente tenéis que tener instalados el compilador de
Scala y vuestro editor favorito. Opcionalmente, también podéis añadir en vuestro classpath las librerías [scalaz](https://github.com/scalaz/scalaz/tree/v7.2.0-M5) y [cats](https://github.com/non/cats/tree/v0.3.0) que utilizaremos en la última parte del workshop.

Para instalar Scala (bien individualmente, o a través de alguno de sus IDEs) podéis seguir las instrucciones en [scala-lang.org](http://www.scala-lang.org/download/). 

Alternativamente, podéis seguir los siguientes pasos (preferiblemente, antes de venir al workshop para evitar congestiones en la red): 

- Instalar Java y [git](https://git-scm.com)
- Clonar este repositorio: 
`git clone https://github.com/hablapps/fpinscala-workshop-codemotion-15.git`.
- Acceder a tu repositorio local: 
`cd fpinscala-workshop-codemotion-15`.
- Actualizar las dependencias: 
`./sbt update`.

El último comando utiliza la herramienta de build `sbt` para actualizar las dependencias del proyecto, incluyendo el compilador de Scala y las librerías scalaz y cats.

### Organización del workshop

Con la salvedad de unas pocas transparencias, durante el workshop haremos uso principalmente del editor de texto y la consola de Scala. 

Concretamente, una vez realizados los pasos anteriores, el siguiente comando permite compilar los ficheros fuentes que editéis dentro del repositorio (utilizando Sublime, Atom, emacs, vim, etc.), y una vez compilados arrancar la consola de comandos de Scala:

- `./sbt ~console`

La virgulilla en el comando anterior nos permite quedar a la espera de nuevos cambios en los fuentes del proyecto una vez salgamos de la REPL de Scala, sin necesidad de arrancar de nuevo la herramienta de build. Durante el workshop, esta facilidad será utilizada con asiduidad.

