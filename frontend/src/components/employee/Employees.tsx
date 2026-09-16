import type {EmployeeModel} from "../models/EmployeeModel.ts";
import {useState} from "react";
import {useAutoScrollToTop} from "../utils/ComponentsFunctions.tsx";
import SearchBar from "../SearchBar.tsx";
import EmployeeCard from "./EmployeeCard.tsx";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type EmployeeProps = {
    language: string;
    role: string;
    employees: EmployeeModel[];
}

export default function Employees(props:Readonly<EmployeeProps>){
    useAutoScrollToTop();
    const navigate = useNavigate();

    const [searchQuery, setSearchQuery] = useState<string>("");
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    function handleAddNewClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/employees/add-new-employee`);
    }


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

    const filteredEmployees = filterEmployees(props.employees, searchQuery);

    return(
        <>
            <h2>Employees</h2>

            <div className={"search-add-new-button"}>
                <SearchBar
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                />
                <button className="button-blue" onClick={handleAddNewClick}>{translatedInfo["New Employee"][props.language]}</button>
            </div>

            {showNoPermission && (
                <NoPermissionPopup
                    language={props.language}
                    onClose={() => setShowNoPermission(false)}
                />
            )}


            <div className="employee-card-container">
                {filteredEmployees.map((employee) => (
                    <EmployeeCard key={employee.id} employee={employee} language={props.language} />
                ))}
            </div>
        </>
    )
}