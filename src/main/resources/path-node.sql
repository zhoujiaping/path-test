drop table if exists path_node;
create table path_node(
	id serial8 primary key,
	name varchar,
	path varchar unique not null,
	seq int4
);
insert into path_node(id,name,path,seq)values(0,'root','',1);