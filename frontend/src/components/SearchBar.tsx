import {translatedInfo} from "./utils/TranslatedInfo.ts";
import "./styles/SearchBar.css"

type SearchBarProps = {
    searchQuery: string;
    setSearchQuery: (value: string) => void;
    language: string;
    // Optional: weitere Filter der Seite (z. B. Gerätetyp), die mit zurückgesetzt werden
    hasActiveFilters?: boolean;
    onReset?: () => void;
}

export default function SearchBar(props: Readonly<SearchBarProps>) {

    const isFiltered = props.searchQuery !== "" || props.hasActiveFilters === true;

    function handleReset() {
        props.setSearchQuery("");
        props.onReset?.();
    }

    return (
        <div className="search-bar search-bar-row">
            <input
                type="text"
                placeholder="Search by Name or other fields..."
                value={props.searchQuery}
                onChange={(e) => props.setSearchQuery(e.target.value)}
                className="search-input"
            />

            <button
                onClick={handleReset}
                className={isFiltered ? "button-group-button" : "button-grey-search"}
            >
                {translatedInfo["Reset Filters"][props.language]}
            </button>
        </div>
    );
}