package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Employee (
   id: Pk[Long] = NotAssigned,
   name: String,
   age:Number,
   gender:String,
   addressId:Option[Long]
)

case class Address (
   id: Pk[Long] = NotAssigned,
   street: String,
   city:String,
   postcode:String
)

object Employee {

  /**
   * Parse a Employee from a ResultSet
   */
  val simple = {
     get[Pk[Long]]("employee.id") ~
     get[String]("employee.name") ~
     get[Int]("employee.age") ~
     get[String]("employee.gender") ~
     get[Option[Long]]("employee.address_id") map {
        case id~name~age~gender~addressId => Employee(id, name, age, gender, addressId)
     }
  }

  /**
   * Parse a (Employee,Address) from a ResultSet
   */
  val withAddress = Employee.simple ~ Address.simple  map {
     case employee~address => (employee,address)
  }

  /**
   *  Find an employee
   *
   *  @param id - Id of the Employee
   *
   */
  def findById(id: Long): List[(Employee, Address)] = {
     DB.withConnection { implicit connection =>
        SQL("select * from employee join address on employee.address_id = address.id where employee.id = {id}").on('id -> id).as(Employee.withAddress *)
     }
  }

  /**
   *  Find all the employees
   */
  def all(): List[(Employee, Address)]= {
     DB.withConnection { implicit connection =>
        SQL("select * from employee join address on " +
          "employee.address_id = address.id").as(Employee.withAddress *)
     }
  }

  /**
   *  Create an employee
   *
   *  @param name - Name of the employee
   *  @param age - Age of the employee
   *  @param gender - Gender of the employee
   *  @param street - Street of the employee
   *  @param city - City of the employee
   *  @param postcode - Postcode of the employee
   *
   */
  def create(name:String,  age:Number, gender:String,  street:String, city:String, postcode:String): Unit = {

     DB.withTransaction { implicit connection =>

        SQL("insert into address(street,city,postcode) " +
                "values ({street}, {city}, {postcode})").on(
           'street -> street,
           'city -> city,
           'postcode -> postcode
        ).executeUpdate()

        val addressId:Long = SQL("select id from address " +
                "order by id desc limit 1").as(scalar[Long].single)

        SQL("insert into employee(name,age,gender, address_id) " +
                "values ({name}, {age}, {gender}, {address_id})").on(
           'name -> name,
           'age -> age,
           'gender -> gender,
           'address_id -> addressId
        ).executeUpdate()
    }
  }

  /**
   *  Update an employee
   *
   *  @param id - Id of the employee
   *  @param name - Name of the employee
   *  @param age - Age of the employee
   *  @param gender - Gender of the employee
   *  @param street - Street of the employee
   *  @param city - City of the employee
   *  @param postcode - Postcode of the employee
   *
   */
  def update(id: Long, name:String,  age:Number, gender:String,  street:String, city:String, postcode:String):Unit = {
     DB.withTransaction { implicit connection =>
        SQL(
           """
              update employee
              set name = {name}, age = {age}, gender = {gender}
              where id = {id}
           """
        ).on(
           'id -> id,
           'name -> name,
           'age -> age,
           'gender -> gender
        ).executeUpdate()

        SQL(
           """
              update address
              set street = {street}, city = {city}, postcode = {postcode}
              where id = (select address_id from employee where id = {id})
           """
        ).on(
          'id -> id,
          'street -> street,
          'city -> city,
          'postcode -> postcode
        ).executeUpdate()

     }
  }

  /**
   *  Delete an employee
   *
   *  @param id - Id of the employee
   */
  def delete(id:Long){
     DB.withConnection { implicit connection =>
       SQL("delete from employee where id = {id}").on('id -> id).executeUpdate()
     }
  }

}


object Address {

  /**
   * Parse a Address from a ResultSet
   */
  val simple = {
      get[Pk[Long]]("address.id") ~
      get[String]("address.street") ~
      get[String]("address.city") ~
      get[String]("address.postcode") map {
        case id~street~city~postcode => Address(id, street, city, postcode)
      }
  }

  /**
   *  Find all addresses
   */
  def all(): List[Address] = DB.withConnection { implicit connection =>
     SQL("select * from address").as(Address.simple *)
  }

}