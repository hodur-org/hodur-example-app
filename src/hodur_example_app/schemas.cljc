(ns hodur-example-app.schemas)

(def shared
  '[^:graphviz/tag
    default

    ^{:datomic/tag true
      :lacinia/tag true}
    Employee
    [^{:type String
       :datomic/tag true
       :lacinia/tag true
       :datomic/unique :db.unique/identity}
     email

     ^{:type String
       :datomic/tag true
       :lacinia/tag true}
     first-name

     ^{:type String
       :datomic/tag true
       :lacinia/tag true}
     last-name

     ^{:type String
       :lacinia/tag true
       :lacinia/resolve :employee/full-name-resolver
       :lacinia->datomic.field/depends-on [:employee/first-name
                                           :employee/last-name]}
     full-name
     
     ^{:type Employee
       :datomic/tag true
       :lacinia/tag true
       :optional true}
     supervisor

     ^{:type EmployeeList
       :lacinia/tag true
       :lacinia/tag-recursive true
       :lacinia->datomic.field/reverse-lookup :employee/supervisor}
     reportees
     [^{:type Integer
        :lacinia/tag true
        :optional true
        :default 0
        :lacinia->datomic.param/offset true}
      offset
      ^{:type Integer
        :lacinia/tag true
        :optional true
        :default 50
        :lacinia->datomic.param/limit true}
      limit]
     
     ^{:type ProjectList
       :lacinia/tag true
       :lacinia/tag-recursive true
       :lacinia->datomic.field/lookup :employee/projects}
     projects
     [^{:type Integer
        :optional true
        :default 0 
        :lacinia->datomic.param/offset true}
      offset
      ^{:type Integer
        :optional true
        :default 50
        :lacinia->datomic.param/limit true}
      limit]

     ^{:type Project
       :datomic/tag true
       :cardinality [0 n]}
     projects]

    ^{:datomic/tag-recursive true
      :lacinia/tag-recursive true}
    Project
    [^{:type ID
       :datomic/unique :db.unique/identity}
     uuid

     ^{:type String}
     name

     ^{:type String
       :lacinia/tag true
       :datomic/tag true}
     description]])

(def lacinia-pagination
  '[^:graphviz/tag
    default

    ^{:lacinia/tag-recursive true}
    ProjectList
    [^Integer
     total-count
     
     ^PageInfo
     page-info

     ^{:type Project
       :optional true
       :cardinality [0 n]}
     nodes]

    ^{:lacinia/tag-recursive true}
    EmployeeList
    [^Integer
     total-count
     
     ^PageInfo
     page-info

     ^{:type Employee
       :cardinality [0 n]}
     nodes]
    
    ^{:lacinia/tag-recursive true}
    PageInfo
    [^{:type Integer}
     total-pages

     ^{:type Integer}
     current-page

     ^{:type Integer}
     page-size
     
     ^{:type Integer}
     current-offset

     ^{:type Boolean}
     has-next
     
     ^{:type Integer}
     next-offset

     ^{:type Boolean}
     has-prev
     
     ^{:type Integer}
     prev-offset]])

(def lacinia-query
  '[^:graphviz/tag
    default

    ^{:lacinia/tag-recursive true
      :lacinia/query true}
    QueryRoot
    [^{:type Employee
       :lacinia->datomic.query/type :one}
     employee
     [^{:type String
        :lacinia->datomic.param/lookup-ref :employee/email
        :lacinia->datomic.param/transform hodur-example-app.resolvers/transform-email}
      email]

     ^{:type EmployeeList
       :lacinia->datomic.query/type :many}
     employees
     [^{:type String
        :optional true
        :lacinia->datomic.param/filter-builder hodur-example-app.resolvers/build-employee-name-search-where}
      name-search
      ^{:type Integer
        :optional true
        :default 0 
        :lacinia->datomic.param/offset true}
      offset
      ^{:type Integer
        :optional true
        :default 50
        :lacinia->datomic.param/limit true}
      limit]

     ^{:type Project
       :lacinia->datomic.query/type :one}
     project
     [^{:type String
        :lacinia->datomic.param/lookup-ref :project/uuid}
      uuid]]])
