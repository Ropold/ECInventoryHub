import type {EmployeeModel} from "../models/EmployeeModel.ts";
import {useEffect, useState} from "react";
import {useAutoScrollToTop} from "../utils/ComponentsFunctions.tsx";
import SearchBar from "../SearchBar.tsx";
import EmployeeCard from "./EmployeeCard.tsx";

type EmployeeProps = {
    language: string;
    employees: EmployeeModel[];
}

export default function Employees(props:Readonly<EmployeeProps>){
    useAutoScrollToTop();

    const [searchQuery, setSearchQuery] = useState<string>("");
    const [filteredEmployees, setFilteredEmployees] = useState<EmployeeModel[]>([]);


    function filterEmployees(employees: EmployeeModel[], query: string): EmployeeModel[] {
        if (!employees) return [];

        const searchQuery = query.toLowerCase();

        return employees.filter(employee => {
            return (
                employee.name.toLowerCase().includes(searchQuery) ||
                employee.personnelNumber?.toLowerCase().includes(searchQuery) ||
                employee.email?.toLowerCase().includes(searchQuery) ||
                employee.phone?.toLowerCase().includes(searchQuery) ||
                employee.address?.toLowerCase().includes(searchQuery) ||
                employee.department.toLowerCase().includes(searchQuery) ||
                employee.id.toLowerCase().includes(searchQuery)
            );
        });
    }

    useEffect(() => {
        setFilteredEmployees(filterEmployees(props.employees, searchQuery));
    }, [searchQuery, props.employees]);

    return(
        <>
            <h2>Employees</h2>
            <SearchBar
                searchQuery={searchQuery}
                setSearchQuery={setSearchQuery}
            />

            <div className="country-company-card-container">
                {filteredEmployees.map((employee) => (
                    <EmployeeCard key={employee.id} employee={employee} language={props.language} />
                ))}
            </div>
        </>
    )
}