
/** 
  Las funciones puras solo se encargan de determinar qué efectos hay que 
  ejecutar, no de ejecutarlos; de esto último se encarga el intérprete. 
  El resultado de la función pura puede entenderse como un "programa" escrito
  en un lenguaje de efectos.
*/
object LanguagesAndInterpreters { 

  /* Impure program */

  def greetings(): Unit =
    println("Hello codemotion!")

  /* Functional solution */

  object Fun{

    // Program

    def greetings(): Print = 
      Print("Hello, codemotion1")

    // Language
    type IOProgram = Print

    case class Print(msg: String)

    // Interpreter
    def run(program: IOProgram): Unit = 
      program match {
        case Print(msg) => println(msg)
      }

  }

  /* OO Solution */

  object OO{

    // Program
    trait Program{ self: IO => 
      def greetings(): Unit = 
        print("Hello, codemotion!")
    }

    // Language
    trait IO{
      def print(msg: String): Unit
    }

    // Interpreter

    trait ConsoleIO extends IO{
      def print(msg: String): Unit = 
        println(msg)
    }

    object program extends Program with ConsoleIO

  }

}

/**
  Los programas que devuelve la función pura están parametrizados con respecto
  al tipo de valores que se obtendrá tras la ejecución del programa por parte del
  intérprete.
*/
object ParameterizedPrograms{ 

  /* Impure program */

  def feedback(): String =
    readLine

  /* Functional solution */

  object Fun{

    // Program

    def feedback(): IOProgram[String] = 
      Read()

    // Language
    type IOProgram[T] = IOEffect[T]

    sealed trait IOEffect[T]
    case class Print(msg: String) extends IOEffect[Unit]
    case class Read() extends IOEffect[String]

    // Interpreter
    def run[T](program: IOProgram[T]): T = 
      program match {
        case Print(msg) => println(msg)
        case Read() => readLine
      }

  }

  /* OO Solution */

  object OO{

    // Program
    trait Program{ self: IO => 
      def feedback(): String = 
        read
    }

    // Language
    trait IO{
      def print(msg: String): Unit
      def read(): String
    }

    // Interpreter

    trait ConsoleIO extends IO{
      def print(msg: String): Unit = 
        println(msg)
      def read(): String = 
        readLine
    }

    object program extends Program with ConsoleIO

  }

}

/**
  Si quereremos purificar funciones que pretendan realizar más de un efecto, 
  nuestro lenguaje de efectos debe darnos la posibilidad de secuenciar programas.
*/
object SequencingPrograms{ 

  /* Impure program */

  def feedback(): String = {
    println("¿Qué tal?")
    readLine
  }

  /* Functional solution */

  object Fun{

    // Program

    def feedback(): IOProgram[String] = 
      Sequence(
        Effect(Print("¿Qué tal?")),
        Effect(Read())
      )

    // Language
    sealed trait IOProgram[T]
    case class Effect[T](effect: IOEffect[T])
      extends IOProgram[T]
    case class Sequence[S,T](
      program1: IOProgram[S],
      program2: IOProgram[T]
    ) extends IOProgram[T]

    sealed trait IOEffect[T]
    case class Print(msg: String) extends IOEffect[Unit]
    case class Read() extends IOEffect[String]

    // Interpreter
    def runEffect[T](effect: IOEffect[T]): T = 
      effect match {
        case Print(msg) => println(msg)
        case Read() => readLine
      }

    def run[T](program: IOProgram[T]): T = 
      program match {
        case Effect(effect) => runEffect(effect)
        case Sequence(p1,p2) => 
          run(p1)
          run(p2)
      }

  }

  /* OO Solution */

  object OO{

    // Program
    trait Program{ self: IO => 
      def feedback(): String = {
        print("qué tal?")
        read
      }
    }

    // Language
    trait IO{
      def print(msg: String): Unit
      def read(): String
    }

    // Interpreter

    trait ConsoleIO extends IO{
      def print(msg: String): Unit = 
        println(msg)
      def read(): String = 
        readLine
    }

    object program extends Program with ConsoleIO

  }

}

/** 
  En la secuenciación de efectos es muy común que los efectos a ejecutar
  dependan de la interpretación de los efectos anteriores. 
*/
object ContextDependentPrograms { 

  /* Impure program */

  def echo: Unit = {
    val msg = readLine
    println(msg)
  }

  /* Functional solution */

  object Fun{

    // Program

    def echo: IOProgram[Unit] =
      Sequence(
        Effect(Read()),
        (msg: String) => Effect(Print(msg))
      )

    // Language

    sealed trait IOProgram[T]
    case class Effect[T](effect: IOEffect[T]) extends IOProgram[T]
    case class Sequence[S,T](
      program1: IOProgram[S], 
      cont: S => IOProgram[T]
    ) extends IOProgram[T]

    sealed trait IOEffect[T]
    case class Print(msg: String) extends IOEffect[Unit]
    case class Read() extends IOEffect[String]

    // Interpreter

    object Interpreter {
      def runEffect[T](effect: IOEffect[T]): T =
        effect match {
          case Print(msg) => println(msg)
          case Read() => readLine
        }

      def run[T](program: IOProgram[T]): T = 
        program match {
          case Effect(effect) => runEffect(effect)
          case Sequence(p1,cont) => 
            val r = run(p1)
            run(cont(r))
        }
    }
  }

  /* OO Solution */

  object OO{

    // Program

    trait Program{ self: IO => 
      def echo: Unit = {
        val msg: String = read
        print(msg)
      }
    }

    // Language

    trait IO{
      def print(msg: String): Unit
      def read: String
    }

    // Interpreter

    trait ConsoleIO extends IO{
      def print(msg: String): Unit = 
        println(msg)
      def read: String = 
        readLine
    }

    object program extends Program with ConsoleIO

  }

}

/**
  El resultado de nuestros programas de efectos puede ser una función 
  de la interpretación de los efectos individuales.
*/
object PurePrograms { 

  /* Impure program */

  def autorized: Boolean = {
    println("user:")
    val user = readLine
    println("password:")
    val pw = readLine
    user=="me" && pw=="hola123"
  }

  /* Functional solution */

  object Fun{

    // Program

    def authorized: IOProgram[Boolean] =
      Sequence(Effect(Print("user:")), (_: Unit) => 
        Sequence(Effect(Read()), (user: String) => 
          Sequence(Effect(Print("password:")), (_: Unit) => 
            Sequence(Effect(Read()), (pw: String) => 
              Value(user=="me" && pw=="hola123")
            )
          )
        )
      )

    // Language

    sealed trait IOProgram[T]

    case class Effect[T](effect: IOEffect[T])
      extends IOProgram[T]

    case class Sequence[S,T](
      program1: IOProgram[S], 
      cont: S => IOProgram[T]
    ) extends IOProgram[T]
    
    case class Value[T](t: T) 
      extends IOProgram[T]

    sealed trait IOEffect[T]
    case class Print(msg: String) extends IOEffect[Unit]
    case class Read() extends IOEffect[String]

    // Interpreter

    object Interpreter {
      def runEffect[T](effect: IOEffect[T]): T =
        effect match {
          case Print(msg) => println(msg)
          case Read() => readLine
        }

      def run[T](program: IOProgram[T]): T = 
        program match {
          case Effect(effect) => runEffect(effect)
          case Sequence(p1,cont) => 
            val r = run(p1)
            run(cont(r))
          case Value(v) => v
        }
    }
  }

  /* OO Solution */

  object OO{

    // Program

    trait Program{ self: IO => 
      def authorized: Boolean = {
        print("user:")
        val user = read
        print("password:")
        val pw = read
        user=="me" && pw=="hola123"
      }
    }

    // Language

    trait IO{
      def print(msg: String): Unit
      def read: String
    }

    // Interpreter

    trait ConsoleIO extends IO{
      def print(msg: String): Unit = 
        println(msg)
      def read: String = 
        readLine
    }

    object program extends Program with ConsoleIO

  }

}

/**
  El lenguaje de efectos anterior tiene ya toda la expresividad requerida
  para escribir cualquier programa imperativo (monádico), pero es muy costoso.
  En esta sección se introducen los smart-constructors y las for-comprehensions
  como técnicas de azúcar sintáctico.
*/
object SyntacticSugar { 

  /* Impure program */

  def autorized: Boolean = {
    println("user:")
    val user = readLine
    println("password:")
    val pw = readLine
    user=="me" && pw=="hola123"
  }

  /* Functional solution */

  object Fun{

    // Program
    import IOProgram._

    def authorized: IOProgram[Boolean] = for{
      _    <- print("user:")
      user <- read()
      _    <- print("password:")
      pw   <- read()
    } yield user=="me" && pw=="hola123"
            

    // Language

    sealed trait IOProgram[T]{
      def flatMap[S](cont: T=>IOProgram[S]): 
        IOProgram[S] = 
          Sequence(this, cont)
      def map[S](f: T => S): IOProgram[S] = 
        flatMap(t => Value(f(t)))
    }

    case class Effect[T](effect: IOEffect[T])
      extends IOProgram[T]

    case class Sequence[S,T](
      program1: IOProgram[S], 
      cont: S => IOProgram[T]
    ) extends IOProgram[T]
    
    case class Value[T](t: T) 
      extends IOProgram[T]

    object IOProgram{
      def print(msg: String): IOProgram[Unit] = 
        Effect(Print(msg))
      def read(): IOProgram[String] = 
        Effect(Read())
    }

    sealed trait IOEffect[T]
    case class Print(msg: String) extends IOEffect[Unit]
    case class Read() extends IOEffect[String]

    // Interpreter

    def runEffect[T](effect: IOEffect[T]): T =
      effect match {
        case Print(msg) => println(msg)
        case Read() => readLine
      }

    def run[T](program: IOProgram[T]): T = 
      program match {
        case Effect(effect) => runEffect(effect)
        case Sequence(p1,cont) => 
          val r = run(p1)
          run(cont(r))
        case Value(v) => v
      }
  }
}

/**
  ¿Cómo se comportan ambas soluciones ante cambios en la interpretación 
   del lenguaje? Respuesta: en la solución funcional los programas dependen únicamente 
   de la API del lenguaje, y el intérprete tiene su propia signatura, por lo que
   los cambios de infraestructura no afectan en absoluto a la lógica de negocio (que 
   es lo que se programa funcionalmente). En cambio, en la solución OO la barrera de
   abstracción está formado por interfaces que se utilizan tanto por el intérprete como
   por los programas de negocio. Esta compartición es la que provoca fricciones entre
   ambos tipos de código: la lógica de negocio y la infraestructura.
*/
object Decoupling { 

  /* Impure program */

  def autorized: Boolean = {
    println("user:")
    val user = readLine
    println("password:")
    val pw = readLine
    user=="me" && pw=="hola123"
  }

  /* Functional solution */

  object Fun{

    // Program
    import IOProgram._

    def authorized: IOProgram[Boolean] = for{
      _    <- print("user:")
      user <- read()
      _    <- print("password:")
      pw   <- read()
    } yield user=="me" && pw=="hola123"
            

    // Language

    sealed trait IOProgram[T]{
      def flatMap[S](cont: T=>IOProgram[S]): 
        IOProgram[S] = 
          Sequence(this, cont)
      def map[S](f: T => S): IOProgram[S] = 
        flatMap(t => Value(f(t)))
    }

    case class Effect[T](effect: IOEffect[T])
      extends IOProgram[T]

    case class Sequence[S,T](
      program1: IOProgram[S], 
      cont: S => IOProgram[T]
    ) extends IOProgram[T]
    
    case class Value[T](t: T) 
      extends IOProgram[T]

    object IOProgram{
      def print(msg: String): IOProgram[Unit] = 
        Effect(Print(msg))
      def read(): IOProgram[String] = 
        Effect(Read())
    }

    sealed trait IOEffect[T]
    case class Print(msg: String) extends IOEffect[Unit]
    case class Read() extends IOEffect[String]

    // Interpreter

    object Interpreter{

      def runEffect[T](effect: IOEffect[T]): T =
        effect match {
          case Print(msg) => println(msg)
          case Read() => readLine
        }

      def run[T](program: IOProgram[T]): T = 
        program match {
          case Effect(effect) => runEffect(effect)
          case Sequence(p1,cont) => 
            val r = run(p1)
            run(cont(r))
          case Value(v) => v
        }
    }

    // Otro intérprete

    object AsyncInterpreter{
      import scala.concurrent.{Future, ExecutionContext}, 
        ExecutionContext.Implicits.global
      
      def runEffect[T](effect: IOEffect[T]): Future[T] =
        effect match {
          case Print(msg) => Future(println(msg))
          case Read() => Future(readLine)
        }
      
      def run[T](program: IOProgram[T]): Future[T] = 
        program match {
          case Effect(effect) => 
            runEffect(effect)
          case Sequence(p1,cont) => 
            for{
              r <- run(p1)
              s <- run(cont(r))
            } yield s
          case Value(v) => 
            Future(v)
        }
    }

  }


  /* OO Solution */

  object OO{

    // Program

    // ¡Esto no tiene nada que ver con la lógica de negocio!
    import scala.concurrent.{Future, ExecutionContext}, 
      ExecutionContext.Implicits.global

    trait Program{ self: IO => 
      def authorized: Future[Boolean] = for {
        _    <- print("user:")
        user <- read
        _    <- print("password:")
        pw   <- read
      } yield user=="me" && pw=="hola123"
    }

    // Language

    trait IO{
      def print(msg: String): Future[Unit] // ¡Nos vemos obligados a cambiar el interfaz!
      def read: Future[String]
    }

    // Interpreter

    trait ConsoleIO extends IO{
      def print(msg: String): Future[Unit] = 
        Future(println(msg))
      def read: Future[String] = 
        Future(readLine)
    }

    object program extends Program with ConsoleIO

  }
}

/** 
  Mediante las librerías Scalaz o cats y el uso de FreeMonads, podemos 
  reducir las líneas de código de la solución funcional significativamente.
*/
object UsingScalaz{
  import scalaz._, Scalaz._

  /* Impure program */

  def autorized: Boolean = {
    println("user:")
    val user = readLine
    println("password:")
    val pw = readLine
    user=="me" && pw=="hola123"
  }

  /* Functional solution */

  object Fun{

    // Program
    import IOProgram._

    def authorized: IOProgram[Boolean] = for{
      _    <- print("user:")
      user <- read()
      _    <- print("password:")
      pw   <- read()
    } yield user=="me" && pw=="hola123"

    // Language

    type IOProgram[T] = Free[IOEffect,T]
    
    object IOProgram{
      def print(msg: String): IOProgram[Unit] = 
        Free.liftF(Print(msg))

      def read(): IOProgram[String] = 
        Free.liftF(Read())
    }

    sealed trait IOEffect[T]
    case class Print(msg: String) extends IOEffect[Unit]
    case class Read() extends IOEffect[String]

    // Interpreter

    object runEffect extends ~>[IOEffect, Id] {
      def apply[A](effect: IOEffect[A]): A = effect match {
        case Print(msg) => println(msg)
        case Read() => readLine
      }
    }

    def run[T](program: IOProgram[T]): T = 
      program.foldMap(runEffect)
  } 
}