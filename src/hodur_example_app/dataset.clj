(ns hodur-example-app.dataset)

(def seed [{:db/id "tl"
            :employee/email "tl@work.co"
            :employee/first-name "Tiago"
            :employee/last-name "Luchini"}
           {:employee/email "me@work.co"
            :employee/first-name "Marcelo"
            :employee/last-name "Eduardo"}
           {:employee/email "zeh@work.co"
            :employee/first-name "Zeh"
            :employee/last-name "Fernandes"
            :employee/supervisor "tl"}
           {:employee/email "a@work.co"
            :employee/first-name "A"
            :employee/last-name "Fernandes"
            :employee/supervisor "tl"}
           {:employee/email "b@work.co"
            :employee/first-name "B"
            :employee/last-name "Fernandes"
            :employee/supervisor "tl"}])
