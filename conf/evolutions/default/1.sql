# --- !Ups

set ignorecase true;

CREATE TABLE employee (
  id                             bigint not null auto_increment,
  name                           varchar(255) not null,
  age                            integer,
  gender                         char(1),
  address_id                     bigint,
  constraint pk_employee primary key (id)
);

CREATE TABLE address (
  id                             bigint not null auto_increment,
  street                         varchar(255) not null,
  city                           varchar(255),
  postcode                       varchar(30) not null,
  constraint pk_address primary key (id)
);

alter table employee add constraint fk_employee_address_1 foreign key (address_id) references address (id) on delete restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists address;

drop table if exists employee;

SET REFERENTIAL_INTEGRITY TRUE;
