drop table if exists path_node;
create table path_node(
	id serial8 primary key,
	name varchar,
	path varchar unique not null,
	seq int4
);