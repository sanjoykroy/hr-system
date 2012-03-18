package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import scala.collection.mutable.HashMap

import views.html
import models.Employee

/**
 * This controller is responsible to manage employees' records
 */
object EmployeeController extends Controller {

  /**
   *  Employee form for creating and editing an employee
   */
  val employeeForm = Form(
     tuple(
        "name" -> text,
        "age" -> number,
        "gender" -> text,
        "street" -> text,
        "city" -> text,
        "postcode" -> text
     )
  )

  /**
   * Display the list of employee
   */

  def employees = Action {
     Ok(html.index(Employee.all()))
  }

  /**
   * Display the employee form to create an employee
   */
  def createForm = Action {
    Ok(html.createForm(employeeForm))
  }

  /**
   * Handle the employee form submission
   */
  def save = Action { implicit request =>
     employeeForm.bindFromRequest.fold(
        errors => BadRequest(html.createForm(errors)),
        {
           case (name, age, gender, street, city, postcode) =>
              Employee.create(name, age, gender, street, city, postcode)
              Redirect(routes.Application.index())
        }
     )
  }

  /**
   * Display the employee form to edit
   * @param id - Id of the employee to edit
   */
  def editForm(id: Long) = Action {
     val employeeMap = new HashMap[Int, String]
     Employee.findById(id).map {
        case (employee, address) => {
           employeeMap += 1 -> employee.name
           employeeMap += 2 -> employee.age.toString
           employeeMap += 3 -> employee.gender
           employeeMap += 4 -> address.street
           employeeMap += 5 -> address.city
           employeeMap += 6 -> address.postcode
        }
     }
     Ok(html.editForm(id, employeeForm.fill((employeeMap(1), employeeMap(2).toInt, employeeMap(3), employeeMap(4), employeeMap(5), employeeMap(6)))))
  }

  /**
   * Handle the employee form submission to edit
   * @param id - Id of the employee to be updated
   */
  def update(id: Long) = Action { implicit request =>
     employeeForm.bindFromRequest.fold(
        errors => BadRequest(html.editForm(id, errors)),
        {
           case (name, age, gender, street, city, postcode) =>
              Employee.update(id, name, age, gender, street, city, postcode)
              Redirect(routes.Application.index())
        }
    )
  }

  /**
   * Handle the request to delete an employee
   * @param id - Id of the employee to be deleted
   */
  def delete(id: Long) = Action {
     Employee.delete(id)
     Redirect(routes.Application.index())
  }

}

